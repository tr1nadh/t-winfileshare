package com.example.twinfileshare.service;

import com.example.twinfileshare.listener.AppMediaHttpUploaderProgressListener;
import com.example.twinfileshare.repository.GoogleUserCREDRepository;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.Strings;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import lombok.extern.log4j.Log4j2;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Log4j2
@Service
public class GoogleDriveService {

    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Autowired
    private GoogleUserCREDRepository googleUserCREDRepository;

    @Autowired
    private GoogleAuthorizationService authorizationService;

    @Value("${google.oauth2.client.application-name}")
    private String googleClientAppName;

    private InputStreamContent mediaContent;

    @Autowired
    private ApplicationEventPublisher publisher;

    private AppMediaHttpUploaderProgressListener progressListener;

    @Async
    public CompletableFuture<DriveUploadResponse> uploadFile(String email, java.io.File file) throws IOException {
        if (Strings.isNullOrEmpty(email))
            throw new IllegalStateException("Email cannot be empty or null");

        if (file == null)
            throw new IllegalStateException("File cannot be null");

        var cred = getCredential(email);

        var drive = getDrive(cred);

        var googleFile = getDriveFile(file, drive);

        mediaContent = new InputStreamContent(Files.probeContentType(file.toPath()),
                new BufferedInputStream(new FileInputStream(file)));
        mediaContent.setLength(file.length());

        log.info("Uploading file '" + file.getName() + "' to drive account '" + email + "'");

        var createRequest = drive.files().create(googleFile, mediaContent)
                .setFields("id");

        var mediaHttpUploader = createRequest.getMediaHttpUploader();
        progressListener = new AppMediaHttpUploaderProgressListener(publisher, this);
        mediaHttpUploader.setProgressListener(progressListener);

        var response = mediaHttpUploader.upload(new GenericUrl("https://www.googleapis.com/upload/drive/v3/files?uploadType=resumable"));

        if (isUploadSuccess(response)) {
            var filename = file.getName();
            log.info("File '" + filename + "' successfully uploaded to drive account '" + email + "'");
            var responseMap = getStringObjectMap(response);
            var id = executeShareAnyonePermission(responseMap, drive, filename);
            var link = getSharableLink(drive, id, filename);
            return CompletableFuture.completedFuture(new DriveUploadResponse(isUploadSuccess(response), link));
        }

        return CompletableFuture.completedFuture(new DriveUploadResponse(false, null));
    }

    private String getSharableLink(Drive drive, String id, String filename) throws IOException {
        log.info("Getting... anyone sharable link for the file: " + filename);
        var fileMetadata = drive.files().get(id).setFields("webViewLink").execute();
        return fileMetadata.getWebViewLink();
    }

    private String executeShareAnyonePermission(Map<String, Object> responseMap, Drive drive, String filename) throws IOException {
        var permissions = new Permission();
        permissions.setType("anyone");
        permissions.setRole("reader");

        log.info("Adding... permissions to view anyone via sharable link to file: " + filename);

        var id = responseMap.get("id").toString();
        drive.permissions().create(id, permissions).execute();
        return id;
    }

    private boolean isUploadSuccess(HttpResponse response) {
        return response.getStatusCode() == 200;
    }

    private Map<String, Object> getStringObjectMap(HttpResponse response) throws IOException {
        return new GsonJsonParser().parseMap(response.parseAsString());
    }

    private File getDriveFile(java.io.File file, Drive drive) throws IOException {
        var googleFile = new File();
        googleFile.setName(file.getName());
        googleFile.setParents(List.of(findOrCreateDefFolder(drive)));
        return googleFile;
    }

    private Drive getDrive(Credential cred) {
        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred)
                .setApplicationName(googleClientAppName).build();
    }

    private Credential getCredential(String email) {
        var googleUserCRED = googleUserCREDRepository.findByEmail(email);
        return authorizationService.toGoogleCredential(googleUserCRED);
    }

    private boolean isUploadCancelled;

    public void cancelUpload() {
        if (!isUploadCancelled) {
            progressListener.cancelUpload();
            isUploadCancelled = true;
            return;
        }

        IOUtils.closeQuietly(mediaContent.getInputStream());
        log.info("Drive media upload cancelled");
        isUploadCancelled = false;
    }

    @Value("${google.drive.def-folder}")
    private String driveDefFolder;

    private String findOrCreateDefFolder(Drive drive) throws IOException {
        var foundFolderId = findFolder(drive);
        if (foundFolderId != null) return foundFolderId;

        return createDefFolder(drive);
    }

    private String findFolder(Drive drive) throws IOException {
        log.info("Querying default folder..." + driveDefFolder);

        var query = "mimeType='application/vnd.google-apps.folder' and name='" + driveDefFolder + "'";
        var queryRequest = drive.files().list()
                .setQ(query)
                .setFields("files(id)")
                .setSpaces("drive");

        var result = queryRequest.execute();
        var files = result.getFiles();

        if (files != null && !files.isEmpty())
            return files.get(0).getId();

        log.info("Default folder -> " + driveDefFolder + " not found X");
        return null;
    }

    private String createDefFolder(Drive drive) throws IOException {
        log.info("Creating default folder..." + driveDefFolder);

        File folderMetadata = new File();
        folderMetadata.setName(driveDefFolder);
        folderMetadata.setMimeType("application/vnd.google-apps.folder");

        File folder = drive.files().create(folderMetadata)
                .setFields("id")
                .execute();

        var folderId = folder.getId();

        log.info("Default folder -> " + driveDefFolder + " created");
        return folderId;
    }
}
