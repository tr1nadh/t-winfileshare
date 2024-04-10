package com.example.twinfileshare.modal;

import com.example.twinfileshare.repository.GoogleUserCREDRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GoogleUserCredModal {

    @Autowired
    private GoogleUserCREDRepository googleUserCREDRepository;

    public List<String> getAllEmails() {
        return googleUserCREDRepository.getAllEmails();
    }
}
