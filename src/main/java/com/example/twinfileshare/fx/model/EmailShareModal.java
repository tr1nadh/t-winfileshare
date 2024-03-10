package com.example.twinfileshare.fx.model;

import com.example.twinfileshare.repository.GoogleUserCREDRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailShareModal {

    @Autowired
    private GoogleUserCREDRepository googleUserCREDRepository;

    public List<String> getAllEmails() {
        return googleUserCREDRepository.getAllEmails();
    }
}
