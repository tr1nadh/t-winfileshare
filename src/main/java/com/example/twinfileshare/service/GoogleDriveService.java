package com.example.twinfileshare.service;

import com.example.twinfileshare.repository.GoogleUserCREDRepository;
import com.example.twinfileshare.utility.Strings;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.model.File;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Log4j2
public class GoogleDriveService {

    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Value("${google.oauth2.client.application-name}")
    private String googleClientAppName;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private GoogleUserCREDRepository googleUserCREDRepository;

    @Autowired
    private GoogleAuthorizationService googleAuthorizationService;

    @Autowired
    private GoogleDrive googleDrive;

    @Async
    public CompletableFuture<DriveUploadResponse> uploadAndShareFileWithAnyone(String email, java.io.File file) throws IOException {
        if (Strings.isEmptyOrWhitespace(email))
            throw new IllegalStateException("Email cannot be empty or null");

        if (file == null)
            throw new IllegalStateException("File cannot be null");

        var cred = getCredential(email);

        var parentFolderId = findOrCreateDefFolder(cred);

        var uploadResponse = googleDrive.uploadFile(cred, file, parentFolderId);

        var filename = file.getName();
        var responseMap = parseResponseToMap(uploadResponse);
        var fileId = responseMap.get("id").toString();

        if (isUploadSuccess(uploadResponse)) {
            log.info("File '" + filename + "' successfully uploaded to drive account '" + email + "'");
            googleDrive.enableFileSharingWithAnyone(cred, fileId);
            var sharableLink = googleDrive.getFileWebViewLink(cred, fileId);
            return CompletableFuture.completedFuture(DriveUploadResponse.builder()
                    .id(fileId)
                    .filename(filename)
                    .isUploadSuccess(isUploadSuccess(uploadResponse))
                    .sharableLink(sharableLink)
                    .email(email)
                    .build());
        }

        return CompletableFuture.completedFuture(DriveUploadResponse.builder()
                .id(fileId).filename(filename).build());
    }

    public void cancelCurrentUpload() {
        googleDrive.cancelUpload();
    }

    private boolean isUploadSuccess(HttpResponse response) {
        return response.getStatusCode() == 200;
    }

    private Map<String, Object> parseResponseToMap(HttpResponse response) throws IOException {
        return new GsonJsonParser().parseMap(response.parseAsString());
    }

    private Credential getCredential(String email) {
        var googleUserCRED = googleUserCREDRepository.findByEmail(email);
        return googleAuthorizationService.toGoogleCredential(googleUserCRED);
    }

    public void enableFileSharingWithAnyone(String email, String fileId) throws IOException {
        var cred = getCredential(email);
        googleDrive.enableFileSharingWithAnyone(cred, fileId);
    }

    @Value("${google.drive.def-folder}")
    private String driveDefFolder;

    private String findOrCreateDefFolder(Credential cred) throws IOException {
        var folderId = googleDrive.findFolder(cred, driveDefFolder);
        if (folderId != null) return folderId;

        return googleDrive.createFolder(cred, driveDefFolder);
    }

    public void deleteFile(String email, String fileId) throws IOException {
        if (Strings.isEmptyOrWhitespace(email))
            throw new IllegalStateException("Email cannot be null or empty");

        if (Strings.isEmptyOrWhitespace(fileId))
            throw new IllegalStateException("File id cannot be null or empty");

        var cred = getCredential(email);
        googleDrive.deleteFile(cred, fileId);
    }

    public void deleteFilePermissions(String email, String fileId) throws IOException {
        if (Strings.isEmptyOrWhitespace(email))
            throw new IllegalStateException("Email cannot be empty or null");

        if (Strings.isEmptyOrWhitespace(fileId))
            throw new IllegalStateException("FileId cannot be empty or null");

        var cred = getCredential(email);

        googleDrive.deleteFilePermissions(cred, fileId);
        log.info("Anyone with link permission has been removed to the file id: " + fileId);
    }

    public List<File> fetchFilesFromCloud(String email) throws IOException {
        if (Strings.isEmptyOrWhitespace(email))
            throw new IllegalStateException("Email cannot be empty or null");

        var cred = getCredential(email);
        return googleDrive.fetchFilesFromCloud(cred, driveDefFolder);
    }
}
