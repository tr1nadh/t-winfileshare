package com.example.twinfileshare.service;

import com.example.twinfileshare.repository.GoogleUserCREDRepository;
import com.example.twinfileshare.google.DriveUploadResponse;
import com.example.twinfileshare.utility.Zipper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Log4j2
@Service
public class LinkShareService {

    @Autowired
    private GoogleUserCREDRepository googleUserCREDRepository;

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
        var uploadFuture = driveService.uploadAndShareFileWithAnyone(email, file);

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

        return driveService.uploadAndShareFileWithAnyone(email, file);
    }

    public void cancelUploadFiles() {
        driveService.cancelCurrentUpload();
    }
}
