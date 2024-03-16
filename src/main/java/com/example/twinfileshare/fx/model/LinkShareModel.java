package com.example.twinfileshare.fx.model;

import com.example.twinfileshare.repository.GoogleUserCREDRepository;
import com.example.twinfileshare.service.DriveUploadResponse;
import com.example.twinfileshare.service.GoogleAuthorizationService;
import com.example.twinfileshare.service.GoogleDriveService;
import com.example.twinfileshare.utility.Zipper;
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
public class LinkShareModel {

    @Autowired
    private GoogleAuthorizationService googleAuthorizationService;
    @Autowired
    private GoogleUserCREDRepository googleUserCREDRepository;

    public String getGoogleSignInURL() {
        return googleAuthorizationService.getGoogleAuthorizationURL();
    }

    public List<String> getAllEmails() {
        return googleUserCREDRepository.getAllEmails();
    }

    public void disconnectAccount(String email) throws GeneralSecurityException, IOException {
        googleAuthorizationService.revokeAccount(email);
        googleUserCREDRepository.deleteByEmail(email);
    }

    @Autowired
    private GoogleDriveService driveService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private Zipper zipper;

    public CompletableFuture<DriveUploadResponse> uploadFilesToGoogleDrive(String email, List<File> requiredFiles,
                                                                           String zipName) throws IOException {
        log.info("Uploading to google drive account: " + email);

        zipper.zipFiles(requiredFiles, zipName);

        var file = new File(zipName + ".zip");
        var uploadFuture = driveService.uploadFile(email, file);

        uploadFuture.thenAcceptAsync(isFinished -> deleteUploadedZipFile(file))
                .exceptionallyAsync(ex -> {deleteUploadedZipFile(file); return null;});

        return uploadFuture;
    }

    private void deleteUploadedZipFile(File file) {
        var isUploadedLocalFileDeleted = file.delete();
        if (isUploadedLocalFileDeleted)
            System.out.println("Uploaded local file deleted..." + file.getName());
    }

    public CompletableFuture<DriveUploadResponse> uploadFileToGoogleDrive(String email, File file) throws IOException {

        log.info("Uploading to google drive account: " + email);

        var uploadFuture = driveService.uploadFile(email, file);

        return uploadFuture;
    }

    public void cancelUploadFiles() {
        driveService.cancelUpload();
    }
}
