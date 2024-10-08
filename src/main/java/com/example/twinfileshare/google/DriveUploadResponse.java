package com.example.twinfileshare.google;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DriveUploadResponse {

    private String id;
    private String email;
    private String filename;
    private boolean isUploadSuccess;
    private String sharableLink;
}
