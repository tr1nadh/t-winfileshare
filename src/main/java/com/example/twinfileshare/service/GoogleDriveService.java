package com.example.twinfileshare.service;

import com.example.twinfileshare.listener.AppMediaHttpUploaderProgressListener;
import com.example.twinfileshare.repository.GoogleUserCREDRepository;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.media.MediaHttpUploader;
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

        var mediaHttpUploader = getMediaHttpUploader(createRequest);

        var response = mediaHttpUploader.upload(new GenericUrl("https://www.googleapis.com/upload/drive/v3/files?uploadType=resumable"));

        var filename = file.getName();
        var responseMap = getStringObjectMap(response);

        if (isUploadSuccess(response)) {
            log.info("File '" + filename + "' successfully uploaded to drive account '" + email + "'");
            var fileId = responseMap.get("id").toString();
            executeShareAnyonePermission(fileId, filename, drive);
            var link = getSharableLink(drive, fileId, filename);
            return CompletableFuture.completedFuture(getDriveUploadResponse(fileId, filename, response, link, email));
        }

        return CompletableFuture.completedFuture(DriveUploadResponse.builder().id(responseMap.get("id").toString()).filename(filename).build());
    }

    private DriveUploadResponse getDriveUploadResponse(String id, String filename, HttpResponse response, String link, String email) {
        return DriveUploadResponse.builder().id(id).filename(filename).isUploadSuccess(isUploadSuccess(response)).sharableLink(link).email(email).build();
    }

    private MediaHttpUploader getMediaHttpUploader(Drive.Files.Create createRequest) {
        var mediaHttpUploader = createRequest.getMediaHttpUploader();
        progressListener = new AppMediaHttpUploaderProgressListener(publisher, this);
        mediaHttpUploader.setProgressListener(progressListener);
        return mediaHttpUploader;
    }

    private String getSharableLink(Drive drive, String id, String filename) throws IOException {
        log.info("Getting... anyone sharable link for the file: " + filename);
        var fileMetadata = drive.files().get(id).setFields("webViewLink").execute();
        return fileMetadata.getWebViewLink();
    }

    private void executeShareAnyonePermission(String fileId, String filename, Drive drive) throws IOException {
        var permissions = new Permission();
        permissions.setType("anyone");
        permissions.setRole("reader");

        log.info("Adding... permissions to view anyone via sharable link to file: " + filename);

        drive.permissions().create(fileId, permissions).execute();
    }

    public void fileShareWithAnyone(String fileId, String email, String filename) throws IOException {
        var drive = getDrive(getCredential(email));
        executeShareAnyonePermission(fileId, filename, drive);
    }

    public void deleteFile(String email, String fileId) throws IOException {
        var drive = getDrive(getCredential(email));
        drive.files().delete(fileId).execute();
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
                .setFields("files(id, trashed)")
                .setSpaces("drive");

        var result = queryRequest.execute();
        var files = result.getFiles();

        if (files != null && !files.isEmpty()) {
            var file = files.get(0);
            if (!file.getTrashed())
                return file.getId();
        }

        log.info("Default folder -> " + driveDefFolder + " not found X");
        return null;
    }

    public void deleteFilePermissions(String email, String fileId) throws IOException {
        if (Strings.isNullOrEmpty(email))
            throw new IllegalStateException("Email cannot be empty or null");

        if (Strings.isNullOrEmpty(fileId))
            throw new IllegalStateException("FileId cannot be empty or null");

        var cred = getCredential(email);

        var drive = getDrive(cred);

        drive.permissions().delete(fileId, "anyoneWithLink").execute();
        log.info("Anyone with link permission has been removed to the file id: " + fileId);
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

    public List<File> findFilesFromCloud(String email) throws IOException {
        if (Strings.isNullOrEmpty(email))
            throw new IllegalStateException("email can't be null");

        var cred = getCredential(email);
        var drive = getDrive(cred);
        var folderId = findFolder(drive);
        var query = String.format("'%s' in parents and mimeType != 'application/vnd.google-apps.folder'", folderId);
        var filesInCloud = drive.files().list()
                .setQ(query)
                .setFields("files(id, name, webViewLink)")
                .execute();

        var files = filesInCloud.getFiles();
        if (files.isEmpty()) {
            log.error("No files in the cloud");
            return null;
        }

        return files;
    }
}
