package com.example.twinfileshare.service;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DriveUploadResponse {

    private String id;
    private String filename;
    private boolean isUploadSuccess;
    private String sharableLink;
}
