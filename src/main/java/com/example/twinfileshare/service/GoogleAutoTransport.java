package com.example.twinfileshare.service;

import com.google.api.client.auth.oauth2.Credential;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.Callable;

@Service
public class GoogleAutoTransport {

    public static <T> Commuted<T> commute(Callable<T> com, Credential cred) {

        Credential refreshedCred;
        T result = null;

        try {
            result = com.call();
        } catch (Exception e) {
            if (e.getMessage().contains("401 Unauthorized") || e.getMessage().contains("400 Bad Request")){
                refreshedCred = refreshToken(cred);
                if (refreshedCred != null)
                    return commute(com, refreshedCred);
            }
        }

        var commuted = new Commuted<T>();
        commuted.setCred(cred);
        commuted.setResult(result);
        commuted.setThereResult(result != null);

        return commuted;
    }

    private static Credential refreshToken(Credential credential) {
        boolean isRefreshed;
        try {
            isRefreshed = credential.refreshToken();
        } catch (IOException e) {
            System.out.println("Invalid access token");
            return null;
        }
        if (isRefreshed)
            return credential;

        System.out.println("Access token is not refreshed");
        return null;
    }
}
