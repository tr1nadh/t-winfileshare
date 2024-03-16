package com.example.twinfileshare.service;

import com.example.twinfileshare.entity.GoogleUserCRED;
import com.example.twinfileshare.event.payload.DoubleEmailConnectEvent;
import com.example.twinfileshare.event.payload.UnsuccessfulAuthorizationEvent;
import com.example.twinfileshare.event.payload.UserConnectedEvent;
import com.example.twinfileshare.listener.DriveTokenRefreshListener;
import com.example.twinfileshare.repository.GoogleUserCREDRepository;
import com.example.twinfileshare.utility.Strings;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.gmail.GmailScopes;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Service
@Log4j2
public class GoogleAuthorizationService {

    private final List<String> scopes = List.of(
            UserScopes.USER_INFO_EMAIL,
            UserScopes.USER_INFO_PROFILE,
            DriveScopes.DRIVE,
            GmailScopes.GMAIL_COMPOSE);
    @Value("${google.oauth2.callback-uri}")
    private String callbackURI;
    @Value("${google.oauth2.client.secret-json}")
    private Resource gClientSecret;
    private GoogleAuthorizer googleAuthorizer;
    @Autowired
    private GoogleUserCREDRepository googleUserCREDRepository;

    @PostConstruct
    public void init() throws IOException {
         this.googleAuthorizer = new GoogleAuthorizer(scopes, callbackURI, gClientSecret,
                 new NetHttpTransport(), GsonFactory.getDefaultInstance());
    }

    public String getGoogleAuthorizationURL() {
        return googleAuthorizer.getGoogleAuthorizationOfflineURL();
    }

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void saveToken(String authCode, String scopes) throws IOException, GeneralSecurityException {
        if (Strings.isEmptyOrWhitespace(authCode))
            throw new IllegalStateException("Authorization code cannot be null or empty");

        if (Strings.isEmptyOrWhitespace(scopes))
            throw new IllegalStateException("Scopes cannot be null or empty");

        if (!hasRequiredScopes(Arrays.stream(scopes.split(" ")).toList())) {
            log.error("User have not given required scopes");
            eventPublisher.publishEvent(new UnsuccessfulAuthorizationEvent(this));
            return;
        }

        var response = googleAuthorizer.requestNewToken(authCode);
        var idTokenPayload = googleAuthorizer.verifyIdToken(response.getIdToken());
        var googleUserCRED = GoogleUserCRED.apply(response, idTokenPayload);

        saveToken(checkForDoubleEmail(googleUserCRED));
    }

    private void saveToken(GoogleUserCRED googleUserCRED) {
        googleUserCREDRepository.save(googleUserCRED);
        eventPublisher.publishEvent(new UserConnectedEvent(this, googleUserCRED.getEmail()));
    }

    private GoogleUserCRED checkForDoubleEmail(GoogleUserCRED googleUserCRED) {
        var optionalAccountFromDb = googleUserCREDRepository
                .findById(googleUserCRED.getId());
        optionalAccountFromDb.ifPresent(
                (account) -> {
                    var message = "A drive account already exists with the email: " + account.getEmail();
                    log.warn(message);
                    eventPublisher.publishEvent(new DoubleEmailConnectEvent(this, account, message, googleUserCRED.getEmail()));
                    googleUserCRED.setEmail(account.getEmail());
                }
        );

        return googleUserCRED;
    }

    private boolean hasRequiredScopes(List<String> scopes) {
        return new HashSet<>(scopes).containsAll(this.scopes);
    }

    public void revokeAccount(String email) throws GeneralSecurityException, IOException {
        if (Strings.isNullOrEmpty(email))
            throw new IllegalStateException("email cannot be null or empty");

        var accessToken = googleUserCREDRepository.findAccessTokenByEmail(email);
        var statusCode = googleAuthorizer.revokeAccount(accessToken);

        if (statusCode == 200) log.error("Google account: " + email + " has been revoked");
        else log.error("Error revoking google account: " + email + ", status code: " + statusCode);
    }

    @Value("${google.oauth2.client.id}")
    private String googleClientId;

    @Value("${google.oauth2.client.secret}")
    private String googleClientSecret;

    public Credential toGoogleCredential(GoogleUserCRED googleUserCRED) {
        var cred = googleAuthorizer.buildCredential(googleClientId, googleClientSecret,
                new DriveTokenRefreshListener(googleUserCREDRepository, googleUserCRED));
        cred.setAccessToken(googleUserCRED.getAccessToken());
        cred.setExpiresInSeconds(googleUserCRED.getExpires());
        cred.setRefreshToken(googleUserCRED.getRefreshToken());

        return cred;
    }
}
