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

@Service
public class GoogleDriveService {

    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Autowired
    private GoogleUserCREDRepository googleUserCREDRepository;

    public void uploadFile(String email, java.io.File file) throws IOException {
        if (Strings.isNullOrEmpty(email))
            throw new IllegalStateException("Email cannot be empty or null");

        var cred = googleUserCREDRepository.findByEmail(email).toGoogleCredential();

        var drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred)
                .setApplicationName("${google.oauth2.client.application-name}").build();

        var uploadedFile = drive.files().create(
                new File(),
                new FileContent(Files.probeContentType(file.toPath()), file)
        ).execute();
    }
}
