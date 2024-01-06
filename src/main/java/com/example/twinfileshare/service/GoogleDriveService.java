package com.example.twinfileshare.service;

import com.example.twinfileshare.repository.GoogleUserCREDRepository;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.Strings;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;

@Service
public class GoogleDriveService {

    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Autowired
    private GoogleUserCREDRepository googleUserCREDRepository;

    @Autowired
    private GoogleAuthorizationService authorizationService;

    private int times;

    public void uploadFile(String email, java.io.File file) throws IOException, GeneralSecurityException {
        if (Strings.isNullOrEmpty(email))
            throw new IllegalStateException("Email cannot be empty or null");

        if (times == 3) throw new IllegalStateException("Something went wrong with uploading...");

        var cred = googleUserCREDRepository.findByEmail(email).toGoogleCredential();

        var drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred)
                .setApplicationName("${google.oauth2.client.application-name}").build();

        var googleFile = new File();
        googleFile.setName(file.getName());

        var uploadedFile = new File();

        try {
            uploadedFile = drive.files()
                    .create(googleFile,
                            new FileContent(Files.probeContentType(file.toPath()),
                                    file))
                    .setFields("id").execute();
        } catch (Exception ex) {
            authorizationService.requestNewAccessToken(cred.getRefreshToken());
            uploadFile(email, file);
            times++;
            ex.printStackTrace();
        }

        times = 0;

        System.out.println("Uploaded file Id: " + uploadedFile.getId());
    }
}
