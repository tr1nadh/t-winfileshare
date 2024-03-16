package com.example.twinfileshare.service;

import com.example.twinfileshare.repository.GoogleUserCREDRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class GoogleDriveServiceTest {

    @Autowired
    private GoogleUserCREDRepository googleUserCREDRepository;

    @Autowired
    private GoogleDriveService service;

    @Test
    void uploadFileWhenAccessTokenIsInvalid() throws IOException, ExecutionException, InterruptedException {
        var cred = googleUserCREDRepository.findByEmail("test-email");
        System.out.println(cred);

        var file = new File("test-file");

        var isFileUploaded = service.uploadFile("test-email", file);

        assertTrue(isFileUploaded.get().isUploadSuccess());
    }

    @Autowired
    private GoogleAuthorizer authorizationService;

    @Test
    void deleteAllInDb() {
        googleUserCREDRepository.deleteAll();
    }

    @Test
    void refreshAccessToken() {
        var cred = googleUserCREDRepository.findByEmail("test-email");
        var gCred = authorizationService.toGoogleCredential(cred);
        System.out.println("Previous access token: " + gCred.getAccessToken());

        boolean isAccessTokenRefreshed = false;
        try {
            isAccessTokenRefreshed = gCred.refreshToken();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("is Access token refreshed: " + isAccessTokenRefreshed);

        System.out.println("Refreshed access token: " + gCred.getAccessToken());
    }

    @Test
    void uploadingFile() throws IOException, InterruptedException {
        var file = new File("test-file");
        service.uploadFile("test-email", file);
        Thread.sleep(6_000);
        service.cancelUpload();
    }
}