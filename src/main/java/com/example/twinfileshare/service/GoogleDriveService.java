package com.example.twinfileshare.service;

import com.example.twinfileshare.listener.AppDriveMediaHttpUploaderProgressListener;
import com.example.twinfileshare.repository.GoogleUserCREDRepository;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.Sleeper;
import com.google.api.client.util.Strings;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;

@Log4j2
@Service
public class GoogleDriveService {

    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Autowired
    private GoogleUserCREDRepository googleUserCREDRepository;

    @Autowired
    private GoogleAuthorizationService authorizationService;

    @Value("${google.oauth2.client.application-name}")
    private String googleClientAppName;

    public boolean uploadFile(String email, java.io.File file) throws IOException {
        if (Strings.isNullOrEmpty(email))
            throw new IllegalStateException("Email cannot be empty or null");

        if (file == null)
            throw new IllegalStateException("File cannot be null");

        var cred = getCredential(email);

        var drive = getDrive(cred);

        var googleFile = getDriveFile(file, drive);

        log.info("Uploading file '" + file.getName() + "' to drive account '" + email + "'");

        var uploadedFile = drive.files().create(googleFile,
                        new FileContent(Files.probeContentType(file.toPath()), file))
                .setFields("id").execute();

        log.info("File successfully uploaded '" + file.getName() + "' id -> " + uploadedFile.getId());

        return uploadedFile.getId() != null;
    }

    @Value("${google.drive.def-folder}")
    private String driveDefFolder;

    private String findOrCreateDefFolder(Drive drive) throws IOException {
        var foundFolderId = findFolder(drive);
        if (foundFolderId != null) return foundFolderId;

        return createDefFolder(drive);
    }

    private String findFolder(Drive drive) throws IOException {
        log.info("Querying default folder..." + driveDefFolder);

        var query = "mimeType='application/vnd.google-apps.folder' and name='" + driveDefFolder + "'";
        var queryRequest = drive.files().list()
                .setQ(query)
                .setFields("files(id)")
                .setSpaces("drive");

        var result = queryRequest.execute();
        var files = result.getFiles();

        if (files != null && !files.isEmpty())
            return files.get(0).getId();

        log.info("Default folder -> " + driveDefFolder + " not found X");
        return null;
    }

    private String createDefFolder(Drive drive) throws IOException {
        log.info("Creating default folder..." + driveDefFolder);

        File folderMetadata = new File();
        folderMetadata.setName(driveDefFolder);
        folderMetadata.setMimeType("application/vnd.google-apps.folder");

        File folder = drive.files().create(folderMetadata)
                .setFields("id")
                .execute();

        var folderId = folder.getId();

        log.info("Default folder -> " + driveDefFolder + " created");
        return folderId;
    }
}
