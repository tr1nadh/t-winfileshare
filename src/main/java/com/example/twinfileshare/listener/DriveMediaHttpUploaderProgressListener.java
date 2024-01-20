package com.example.twinfileshare.listener;

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
public class DriveMediaHttpUploaderProgressListener implements MediaHttpUploaderProgressListener {
    @Override
    public void progressChanged(MediaHttpUploader mediaHttpUploader) throws IOException {
        log.info("Drive media upload status: " + mediaHttpUploader.getUploadState());
        log.info("Drive media progress: " + mediaHttpUploader.getProgress());
    }
}
