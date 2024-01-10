package com.example.twinfileshare.service;

import com.example.twinfileshare.repository.GoogleUserCREDRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GoogleDriveServiceTest {

    @Autowired
    private GoogleUserCREDRepository googleUserCREDRepository;

    @Autowired
    private GoogleDriveService service;

    @Test
    void uploadFileWhenAccessTokenIsInvalid() throws GeneralSecurityException, IOException {
        var cred = googleUserCREDRepository.findByEmail("nookadammu2@gmail.com");
        System.out.println(cred);

        var file = new File("C:\\Users\\Trinadh\\IdeaProjects\\t-winfileshare\\pom.xml");

        var isFileUploaded = service.uploadFile("nookadammu2@gmail.com", file);

        assertTrue(isFileUploaded);
    }

    @Autowired
    private GoogleAuthorizationService authorizationService;

    @Test
    void deleteAllInDb() {
        googleUserCREDRepository.deleteAll();
    }

    @Test
    void refreshAccessToken() {
        var cred = googleUserCREDRepository.findByEmail("nookadammu2@gmail.com");
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
}