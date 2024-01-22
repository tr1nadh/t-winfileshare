package com.example.twinfileshare.fx.model;

import com.example.twinfileshare.repository.GoogleUserCREDRepository;
import com.example.twinfileshare.service.GoogleAuthorizationService;
import com.example.twinfileshare.service.GoogleDriveService;
import com.example.twinfileshare.service.utility.Zipper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Log4j2
@Service
public class MainModel {

    @Autowired
    private GoogleAuthorizationService googleAuthorizationService;
    @Autowired
    private GoogleUserCREDRepository googleUserCREDRepository;

    public String getGoogleSignInURL() {
        return googleAuthorizationService.getGoogleSignInURL();
    }

    public List<String> getAllEmails() {
        return googleUserCREDRepository.getAllEmails();
    }

    public void disconnectAccount(String email) throws GeneralSecurityException, IOException {
        googleAuthorizationService.revokeUserWithEmail(email);
        googleUserCREDRepository.deleteByEmail(email);
    }

    private boolean isUploadingActive;

    @Autowired
    private GoogleDriveService driveService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private Zipper zipper;

    public CompletableFuture<Boolean> uploadFilesToGoogleDrive(String email, List<File> requiredFiles,
                                                               String zipName) throws IOException {
        if (isUploadingActive)
            throw new IllegalStateException("An upload is active. Cancel the previous upload to start new.");

        log.info("Uploading to google drive account: " + email);

        zipper.zipFiles(requiredFiles, zipName);

        isUploadingActive = true;

        var file = new File(zipName + ".zip");
        var uploadFuture = driveService.uploadFile(email, file);

        isUploadingActive = false;

        uploadFuture.thenAcceptAsync(isFinished -> deleteUploadedZipFile(file));

        return uploadFuture;
    }

    private void deleteUploadedZipFile(File file) {
        var isUploadedLocalFileDeleted = file.delete();
        if (isUploadedLocalFileDeleted)
            System.out.println("Uploaded local file deleted..." + file.getName());
    }

    public CompletableFuture<Boolean> uploadFileToGoogleDrive(String email, File file) throws IOException, InterruptedException, GeneralSecurityException {
        if (isUploadingActive)
            throw new IllegalStateException("An upload is active. Cancel the previous upload to start new.");

        log.info("Uploading to google drive account: " + email);

        isUploadingActive = true;

        var uploadFuture = driveService.uploadFile(email, file);

        isUploadingActive = false;

        return uploadFuture;
    }

    public void cancelUploadFiles() {
        if (!isUploadingActive) {
            driveService.cancelUpload();
            return;
        }

       log.error("No upload to cancel.");
    }
}
