package com.example.twinfileshare.fx.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MainModelTest {

    @Autowired
    private MainModel mainModel;

    @Test
    void uploadFilesToGoogleDrive() throws GeneralSecurityException, IOException, InterruptedException, ExecutionException {
        List<File> requiredFiles = List.of(
                new File("test-file"),
                new File("test-file"),
                new File("test-file")
        );

        var result = mainModel.uploadFilesToGoogleDrive(
                "test-email",
                requiredFiles,
                "test-files"
        );

        var res = result.get();

        assertTrue(res);
    }
}