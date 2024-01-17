package com.example.twinfileshare.fx.model;

import com.example.twinfileshare.repository.GoogleUserCREDRepository;
import com.example.twinfileshare.service.GoogleAuthorizationService;
import com.example.twinfileshare.service.GoogleDriveService;
import com.example.twinfileshare.service.utility.Zipper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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

    private boolean isUploadCancelled;

    @Autowired
    private GoogleDriveService driveService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private Zipper zipper;

    @Async
    public CompletableFuture<Boolean> uploadFilesToGoogleDrive(String email, List<File> requiredFiles,
                                                               String zipName) throws IOException, InterruptedException, GeneralSecurityException {
        System.out.println("Uploading to google drive account: " + email);

        zipper.zipFiles(requiredFiles, zipName);

        var file = new File(zipName + ".zip");
        driveService.uploadFile(email, file);

        var isUploadedLocalFileDeleted = file.delete();
        if (isUploadedLocalFileDeleted)
            System.out.println("Uploaded local file deleted..." + file.getName());

        return CompletableFuture.completedFuture(true);
    }

    @Async
    public CompletableFuture<Boolean> uploadFileToGoogleDrive(String email, File file) throws IOException, InterruptedException, GeneralSecurityException {
        System.out.println("Uploading to google drive account: " + email);

        driveService.uploadFile(email, file);

        return CompletableFuture.completedFuture(true);
    }

    public void cancelUploadFiles() {
        isUploadCancelled = true;
    }
}
