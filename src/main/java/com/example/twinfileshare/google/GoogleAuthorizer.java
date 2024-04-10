package com.example.twinfileshare.google;

import com.example.twinfileshare.utility.Strings;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialRefreshListener;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.*;
import com.google.api.client.json.JsonFactory;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class GoogleAuthorizer {
    private final HttpTransport httpTransport;
    private final JsonFactory jsonFactory;
    private final String callbackURI;
    private GoogleAuthorizationCodeFlow flow;

    public GoogleAuthorizer(List<String> scopes, String callbackURI, Resource gClientSecret,
    HttpTransport httpTransport, JsonFactory jsonFactory) throws IOException {
        this.httpTransport = httpTransport;
        this.jsonFactory = jsonFactory;
        this.callbackURI = callbackURI;
        GoogleClientSecrets secrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(gClientSecret.getInputStream()));
        flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, secrets, scopes).build();
    }

    public String getGoogleAuthorizationOfflineURL() {
        GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();
        return url.setRedirectUri(callbackURI).setAccessType("offline").build();
    }

    public GoogleTokenResponse requestNewToken(String authCode) throws IOException {
        return flow.newTokenRequest(authCode).setRedirectUri(callbackURI).execute();
    }

    public GoogleIdToken.Payload verifyIdToken(String idToken) throws GeneralSecurityException, IOException {
        if (Strings.isEmptyOrWhitespace(idToken))
            throw new IllegalStateException("Invalid ID token");

        var verifier = new GoogleIdTokenVerifier.Builder(httpTransport, jsonFactory).build();
        var googleIdToken = verifier.verify(idToken);
        if (googleIdToken != null) return googleIdToken.getPayload();
        else throw new IllegalStateException("Cannot authenticate user");
    }

    public int revokeAccount(String accessToken) throws GeneralSecurityException, IOException {
        if (Strings.isNullOrEmpty(accessToken))
            throw new IllegalStateException("Access token cannot be null or empty");

        var url = "https://oauth2.googleapis.com/revoke";
        Map<String, String> data = new HashMap<>();
        data.put("token", accessToken);

        var request = GoogleNetHttpTransport.newTrustedTransport()
                .createRequestFactory()
                .buildPostRequest(new GenericUrl(url), new UrlEncodedContent(data));

        HttpResponse response;
        var statusCode = 0;
        try {
            response = request.execute();
            statusCode = response.getStatusCode();
        } catch (HttpResponseException ex) {
            statusCode = ex.getStatusCode();
            log.error(ex.getStatusMessage());
        }

        return statusCode;
    }

    public Credential buildCredential(String googleClientId, String googleClientSecret,
                                      CredentialRefreshListener credentialRefreshListener) {
        return new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                .setJsonFactory(jsonFactory)
                .setTransport(httpTransport)
                .setClientAuthentication(new ClientParametersAuthentication(
                        googleClientId,
                        googleClientSecret
                ))
                .setTokenServerUrl(new GenericUrl("https://oauth2.googleapis.com/token"))
                .addRefreshListener(credentialRefreshListener)
                .build();
    }
}
