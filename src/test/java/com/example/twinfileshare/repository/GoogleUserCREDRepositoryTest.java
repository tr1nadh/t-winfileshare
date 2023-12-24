package com.example.twinfileshare.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GoogleUserCREDRepositoryTest {

    @Autowired
    private GoogleUserCREDRepository googleUserCREDRepository;

    @Test
    void getAllEmailsFromUserCRED() {
        System.out.println("Printing all emails");
        System.out.println("-----------------------");
        System.out.println(googleUserCREDRepository.getAllEmails());
    }

    @Test
    void getAllIds() {
        System.out.println("Printing all ids");
        System.out.println("-----------------------");
        System.out.println(googleUserCREDRepository.getAllIds());
    }

    @Test
    void deleteByEmail() {
        System.out.println("Delete all ids");
        System.out.println("-----------------------");
        System.out.println(googleUserCREDRepository.getAllEmails());
    }

    @Test
    void findAccessTokenByEmail() {
        System.out.println(googleUserCREDRepository.getAllEmails());
    }
}