package com.example.twinfileshare.fx.model;

import com.example.twinfileshare.event.payload.HandleProgressEvent;
import com.example.twinfileshare.repository.GoogleUserCREDRepository;
import com.example.twinfileshare.service.GoogleAuthorizationService;
import com.example.twinfileshare.service.GoogleDriveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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

    @Async
    public CompletableFuture<Boolean> uploadFilesToGoogleDrive(String email, List<File> allFiles,
                                                      List<String> requiredFileNames) throws IOException, InterruptedException {
        System.out.println("Uploading to google drive account: " + email);

        var progressEvent = HandleProgressEvent.getInstance()
                .setSource(this)
                .start()
                .setTotalRotations(requiredFileNames.size())
                .close();

        publisher.publishEvent(progressEvent);
        for (var file : allFiles) {
            var fileName = file.getName();
            if (requiredFileNames.contains(fileName) && !isUploadCancelled) {
                var itemType = Files.probeContentType(file.toPath());
                System.out.println("File name: " + file.getName() + " ||| file type: " + itemType);
                driveService.uploadFile(email, file);
                publisher.publishEvent(progressEvent.increaseProgress());
            }
            if (isUploadCancelled) {
                System.out.println("Deleting the uploaded files till now");
                isUploadCancelled = false;
                return CompletableFuture.completedFuture(false);
            }
        }

        publisher.publishEvent(progressEvent.completeProgress());
        return CompletableFuture.completedFuture(true);
    }

    public void cancelUploadFiles() {
        isUploadCancelled = true;
    }
}
