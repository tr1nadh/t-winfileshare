package com.example.twinfileshare.service;

import com.google.api.client.auth.oauth2.Credential;
import lombok.Data;

@Data
public class Commuted<T> {

    private T result;
    private boolean isThereResult;
    private Credential cred;
}
