package com.example.twinfileshare.service;

import com.example.twinfileshare.listener.AppMediaHttpUploaderProgressListener;
import com.example.twinfileshare.repository.GoogleUserCREDRepository;
import com.example.twinfileshare.utility.Strings;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import lombok.extern.log4j.Log4j2;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Log4j2
@Service
public class GoogleDrive {

    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Autowired
    private GoogleUserCREDRepository googleUserCREDRepository;

    @Autowired
    private GoogleAuthorizationService googleAuthorizationService;

    @Value("${google.oauth2.client.application-name}")
    private String googleClientAppName;

    private InputStreamContent mediaContent;

    @Autowired
    private ApplicationEventPublisher publisher;

    private AppMediaHttpUploaderProgressListener progressListener;

    @Async
    public HttpResponse uploadFile(Credential cred, java.io.File file, String folderId) throws IOException {
        if (cred == null)
            throw new IllegalStateException("Credential cannot be null");

        if (file == null)
            throw new IllegalStateException("File cannot be null");

        var drive = buildDrive(cred);

        var googleFile = getDriveFile(file, folderId);

        mediaContent = new InputStreamContent(Files.probeContentType(file.toPath()),
                new BufferedInputStream(new FileInputStream(file)));
        mediaContent.setLength(file.length());

        var createRequest = drive.files().create(googleFile, mediaContent)
                .setFields("id");

        var mediaHttpUploader = getMediaHttpUploader(createRequest);

        return mediaHttpUploader.upload(new GenericUrl("https://www.googleapis.com/upload/drive/v3/files?uploadType=resumable"));
    }

    private MediaHttpUploader getMediaHttpUploader(Drive.Files.Create createRequest) {
        var mediaHttpUploader = createRequest.getMediaHttpUploader();
        progressListener = new AppMediaHttpUploaderProgressListener(publisher, this);
        mediaHttpUploader.setProgressListener(progressListener);
        return mediaHttpUploader;
    }

    public String getFileWebViewLink(Credential cred, String fileId) throws IOException {
        var drive = buildDrive(cred);
        var fileMetadata = drive.files().get(fileId).setFields("webViewLink").execute();
        return fileMetadata.getWebViewLink();
    }

    public void enableFileSharingWithAnyone(Credential cred, String fileId) throws IOException {
        var drive = buildDrive(cred);

        var permissions = new Permission();
        permissions.setType("anyone");
        permissions.setRole("reader");

        drive.permissions().create(fileId, permissions).execute();
    }

    public void deleteFile(Credential cred, String fileId) throws IOException {
        if (cred == null)
            throw new IllegalStateException("Credential cannot be null");

        if (Strings.isEmptyOrWhitespace(fileId))
            throw new IllegalStateException("File id cannot be null or empty");

        var drive = buildDrive(cred);
        drive.files().delete(fileId).execute();
    }

    private File getDriveFile(java.io.File file, String parentId) {
        var googleFile = new File();
        googleFile.setName(file.getName());
        googleFile.setParents(List.of(parentId));
        return googleFile;
    }

    private Drive buildDrive(Credential cred) {
        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred)
                .setApplicationName(googleClientAppName).build();
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

    public String findFolder(Credential cred, String folderName) throws IOException {
        if (cred == null)
            throw new IllegalStateException("Credential cannot be null");

        if (Strings.isEmptyOrWhitespace(folderName))
            throw new IllegalStateException("Folder name cannot be empty or null");

        var drive = buildDrive(cred);

        var query = "mimeType='application/vnd.google-apps.folder' and name='" + folderName + "'";
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

        return null;
    }

    public void deleteFilePermissions(Credential cred, String fileId) throws IOException {
        if (cred == null)
            throw new IllegalStateException("Credential cannot be null");

        if (Strings.isEmptyOrWhitespace(fileId))
            throw new IllegalStateException("FileId cannot be empty or null");

        var drive = buildDrive(cred);
        drive.permissions().delete(fileId, "anyoneWithLink").execute();
    }

    public String createFolder(Credential cred, String folderName) throws IOException {
        if (cred == null)
            throw new IllegalStateException("Credential cannot be null");

        if (Strings.isEmptyOrWhitespace(folderName))
            throw new IllegalStateException("Folder name cannot be empty or null");

        File folderMetadata = new File();
        folderMetadata.setName(folderName);
        folderMetadata.setMimeType("application/vnd.google-apps.folder");

        var drive = buildDrive(cred);
        var file = drive.files().create(folderMetadata)
                .setFields("id")
                .execute();

        return file.getId();
    }

    public List<File> fetchFilesFromCloud(Credential cred, String folderName) throws IOException {
        if (cred == null)
            throw new IllegalStateException("Credential cannot be null");

        if (Strings.isEmptyOrWhitespace(folderName))
            throw new IllegalStateException("Folder name cannot be empty or null");

        var drive = buildDrive(cred);
        var folderId = findFolder(cred, folderName);
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
