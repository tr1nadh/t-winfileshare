package com.example.twinfileshare.entity;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "google_user_cred")
public class GoogleUserCRED {

    @Id
    private String id;

    private String email;

    private String accessToken;

    private Long expires;

    private String refreshToken;

    private String shareFolderId;

    public static GoogleUserCRED apply(GoogleTokenResponse response, GoogleIdToken.Payload idTokenPayload) {
        var googleUserCRED = new GoogleUserCRED();
        googleUserCRED.setAccessToken(response.getAccessToken());
        googleUserCRED.setExpires(response.getExpiresInSeconds());
        googleUserCRED.setRefreshToken(response.getRefreshToken());
        googleUserCRED.setEmail(idTokenPayload.getEmail());
        googleUserCRED.setId(idTokenPayload.getSubject());

        return googleUserCRED;
    }

    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public Credential toGoogleCredential() {
        var cred = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                .setJsonFactory(JSON_FACTORY)
                .setTransport(HTTP_TRANSPORT)
                .setClientAuthentication(new ClientParametersAuthentication(
                        "${google.oauth2.client.id}",
                        "${google.oauth2.client.secret}"
                ))
                .setTokenServerUrl(new GenericUrl("https://accounts.google.com/o/oauth2/auth"))
                .build();

        cred.setAccessToken(accessToken);
        cred.setExpiresInSeconds(expires);
        cred.setRefreshToken(refreshToken);

        return cred;
    }
}
