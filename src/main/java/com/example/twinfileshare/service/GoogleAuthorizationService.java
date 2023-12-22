package com.example.twinfileshare.service;

import com.example.twinfileshare.entity.GoogleUserCRED;
import com.example.twinfileshare.repository.GoogleUserCREDRepository;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.DriveScopes;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
public class GoogleAuthorizationService {
    private static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = List.of(
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/userinfo.profile",
            DriveScopes.DRIVE);

    @Value("${google.oauth.callback.uri}")
    private String CALLBACK_URI;

    @Value("${google.client.secret}")
    private Resource gClientSecret;

    @Value("${google.credential.store.path}")
    private Resource credentialsFolder;

    private GoogleAuthorizationCodeFlow flow;

    @PostConstruct
    public void init() throws IOException {
        GoogleClientSecrets secrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(gClientSecret.getInputStream()));
        flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, secrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(credentialsFolder.getFile())).build();
    }

    public String getGoogleSignInURL() {
        GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();
        return url.setRedirectUri(CALLBACK_URI).setAccessType("offline").build();
    }

    @Autowired
    private GoogleUserCREDRepository googleUserCREDRepository;

    public void saveToken(String authCode) throws IOException, GeneralSecurityException {
        if (authCode == null || authCode.isBlank())
            throw new IllegalStateException("Invalid authorization code");

        GoogleTokenResponse response = flow.newTokenRequest(authCode).setRedirectUri(CALLBACK_URI).execute();
        if (!doesResponseHasDriveScope(response))
            log.warn("User have not given drive access");

        var idTokenPayload = verifyIdToken(response.getIdToken());
        var googleUserCRED = GoogleUserCRED.apply(response, idTokenPayload);
        googleUserCREDRepository.save(googleUserCRED);
    }

    private boolean doesResponseHasDriveScope(GoogleTokenResponse response) {
        return response.getScope().contains("drive");
    }

    private GoogleIdToken.Payload verifyIdToken(String idToken) throws GeneralSecurityException, IOException {
        if (idToken == null || idToken.isBlank())
            throw new IllegalStateException("Invalid ID token");

        var verifier = new GoogleIdTokenVerifier.Builder(HTTP_TRANSPORT, JSON_FACTORY).build();
        var googleIdToken = verifier.verify(idToken);
        if (googleIdToken != null) return googleIdToken.getPayload();
        else throw new IllegalStateException("Cannot authenticate user");
    }

    public void revokeUserWithEmail(String email) throws GeneralSecurityException, IOException {
        var url = "https://oauth2.googleapis.com/revoke";
        Map<String, String> data = new HashMap<>();
        data.put("token", googleUserCREDRepository.findAccessTokenByEmail(email));
        data.put("client_id", "${GOOGLE_CLIENT_ID}");
        data.put("client_secret", "${GOOGLE_CLIENT_SECRET_TEXT}");

        var response = GoogleNetHttpTransport.newTrustedTransport()
                .createRequestFactory()
                .buildPostRequest(new GenericUrl(url), new UrlEncodedContent(data))
                .execute();

        var statusCode = response.getStatusCode();
        if (statusCode == 200) System.out.println("User with email: " + email + " has been revoked");
        else System.out.println("Error revoking user: " + email + ", status code: " + statusCode);
    }
}
