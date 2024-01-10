package com.example.twinfileshare.listener;

import com.example.twinfileshare.entity.GoogleUserCRED;
import com.example.twinfileshare.repository.GoogleUserCREDRepository;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialRefreshListener;
import com.google.api.client.auth.oauth2.TokenErrorResponse;
import com.google.api.client.auth.oauth2.TokenResponse;

import java.io.IOException;

public class RefreshListener implements CredentialRefreshListener {

    private final GoogleUserCREDRepository repository;
    private final GoogleUserCRED googleUserCRED;

    public RefreshListener(GoogleUserCREDRepository repository, GoogleUserCRED googleUserCRED) {
        this.repository = repository;
        this.googleUserCRED = googleUserCRED;
    }

    @Override
    public void onTokenResponse(Credential credential, TokenResponse tokenResponse) throws IOException {
        googleUserCRED.setAccessToken(credential.getAccessToken());
        googleUserCRED.setExpires(credential.getExpiresInSeconds());

        var prevRefreshToken = googleUserCRED.getRefreshToken();
        var tokenResponseRefreshToken = credential.getRefreshToken();
        if (!prevRefreshToken.equals(tokenResponseRefreshToken))
            googleUserCRED.setRefreshToken(tokenResponseRefreshToken);

        repository.save(googleUserCRED);
    }

    @Override
    public void onTokenErrorResponse(Credential credential, TokenErrorResponse tokenErrorResponse) throws IOException {
        System.out.println("Token response error: " + tokenErrorResponse.getError());
    }
}
