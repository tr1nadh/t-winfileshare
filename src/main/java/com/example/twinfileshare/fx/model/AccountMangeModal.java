package com.example.twinfileshare.fx.model;

import com.example.twinfileshare.repository.GoogleUserCREDRepository;
import com.example.twinfileshare.service.GoogleAuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Component
public class AccountMangeModal {

    @Autowired
    private GoogleUserCREDRepository googleUserCREDRepository;

    public List<String> getAllEmails() {
        return googleUserCREDRepository.getAllEmails();
    }

    @Autowired
    private GoogleAuthorizationService googleAuthorizationService;

    public void disconnectAccount(String email) throws GeneralSecurityException, IOException {
        googleAuthorizationService.revokeAccount(email);
        googleUserCREDRepository.deleteByEmail(email);
    }

    public String getGoogleSignInURL() {
        return googleAuthorizationService.getGoogleAuthorizationURL();
    }
}
