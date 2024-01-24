package com.example.twinfileshare.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DriveUploadResponse {

    private boolean isUploadSuccess;
    private String sharableLink;
}
