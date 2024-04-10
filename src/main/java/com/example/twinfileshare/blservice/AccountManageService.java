package com.example.twinfileshare.blservice;

import com.example.twinfileshare.repository.GoogleUserCREDRepository;
import com.example.twinfileshare.service.GoogleAuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
public class AccountManageService {

    @Autowired
    private GoogleAuthorizationService googleAuthorizationService;
    @Autowired
    private GoogleUserCREDRepository googleUserCREDRepository;

    public void disconnectAccount(String email) throws GeneralSecurityException, IOException {
        googleAuthorizationService.revokeAccount(email);
        googleUserCREDRepository.deleteByEmail(email);
    }
}
