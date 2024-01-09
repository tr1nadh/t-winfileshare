package com.example.twinfileshare.service;

import com.example.twinfileshare.repository.GoogleUserCREDRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GoogleDriveServiceTest {

    @Autowired
    private GoogleUserCREDRepository googleUserCREDRepository;

    @Test
    void uploadFile() {
        var cred = googleUserCREDRepository.findByEmail("***REMOVED***");
        System.out.println(cred);
    }
}