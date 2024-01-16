package com.example.twinfileshare.service;

import com.example.twinfileshare.entity.GoogleUserCRED;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

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

        var googleUserCRED = googleUserCREDRepository.findByEmail(email);
        var cred = authorizationService.toGoogleCredential(googleUserCRED);

        var drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred)
                .setApplicationName(googleClientAppName).build();

        var googleFile = new File();
        googleFile.setName(file.getName());
        googleFile.setParents(List.of(getDefFolderId(googleUserCRED, drive)));

        var uploadedFile = drive.files().create(googleFile,
                        new FileContent(Files.probeContentType(file.toPath()), file))
                .setFields("id").execute();

        System.out.println("Uploaded file Id: " + uploadedFile.getId());

        return uploadedFile.getId() != null;
    }

    @Value("${google.drive.def-folder}")
    private String driveDefFolder;

    private String getDefFolderId(GoogleUserCRED googleUserCRED, Drive drive) throws IOException {
        var foundFolderId = findFolder(googleUserCRED, drive);
        if (foundFolderId != null) return foundFolderId;

        return createDefFolder(googleUserCRED, drive);
    }

    private String findFolder(GoogleUserCRED googleUserCRED, Drive drive) throws IOException {
        System.out.println("Querying def folder..." + driveDefFolder);

        var query = "mimeType='application/vnd.google-apps.folder' and name='" + driveDefFolder + "'";
        var queryRequest = drive.files().list()
                .setQ(query)
                .setFields("files(id)")
                .setSpaces("drive");

        var result = queryRequest.execute();
        var files = result.getFiles();

        if (files != null && !files.isEmpty()) {
            var folderId = files.get(0).getId();
            googleUserCRED.setShareFolderId(folderId);
            googleUserCREDRepository.save(googleUserCRED);

            return folderId;
        }

        return null;
    }

    private String createDefFolder(GoogleUserCRED googleUserCRED, Drive drive) throws IOException {
        System.out.println("Creating def folder..." + driveDefFolder);

        File folderMetadata = new File();
        folderMetadata.setName(driveDefFolder);
        folderMetadata.setMimeType("application/vnd.google-apps.folder");

        File folder = drive.files().create(folderMetadata)
                .setFields("id")
                .execute();

        var folderId = folder.getId();
        googleUserCRED.setShareFolderId(folderId);
        googleUserCREDRepository.save(googleUserCRED);

        return folderId;
    }
}
