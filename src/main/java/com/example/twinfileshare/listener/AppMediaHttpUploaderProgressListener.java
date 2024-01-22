package com.example.twinfileshare.listener;

import com.example.twinfileshare.event.payload.HandleProgressEvent;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;

import java.io.IOException;

@Log4j2
public class AppMediaHttpUploaderProgressListener implements MediaHttpUploaderProgressListener {

    private final ApplicationEventPublisher publisher;

    public AppMediaHttpUploaderProgressListener(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void progressChanged(MediaHttpUploader mediaHttpUploader) throws IOException {
        log.info("Drive media upload status: " + mediaHttpUploader.getUploadState());
        log.info("Drive media progress: " + mediaHttpUploader.getProgress());
        var progressEvent = new HandleProgressEvent(this);
        progressEvent.setProgress(mediaHttpUploader.getProgress());
        publisher.publishEvent(progressEvent);
    }
}
