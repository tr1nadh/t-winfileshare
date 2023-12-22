package com.example.twinfileshare.entity;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
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

    public static GoogleUserCRED apply(GoogleTokenResponse response, GoogleIdToken.Payload idTokenPayload) {
        var googleUserCRED = new GoogleUserCRED();
        googleUserCRED.setAccessToken(response.getAccessToken());
        googleUserCRED.setExpires(response.getExpiresInSeconds());
        googleUserCRED.setRefreshToken(response.getRefreshToken());
        googleUserCRED.setEmail(idTokenPayload.getEmail());
        googleUserCRED.setId(idTokenPayload.getSubject());

        return googleUserCRED;
    }
}
