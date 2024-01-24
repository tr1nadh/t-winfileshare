package com.example.twinfileshare.listener;

import com.example.twinfileshare.event.payload.HandleProgressEvent;
import com.example.twinfileshare.service.GoogleDriveService;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;

import java.io.IOException;

@Log4j2
public class AppMediaHttpUploaderProgressListener implements MediaHttpUploaderProgressListener {

    private final ApplicationEventPublisher publisher;
    private final GoogleDriveService driveService;

    public AppMediaHttpUploaderProgressListener(ApplicationEventPublisher publisher, GoogleDriveService driveService) {
        this.publisher = publisher;
        this.driveService = driveService;
    }

    private boolean isProgressCancelled;

    @Override
    public void progressChanged(MediaHttpUploader mediaHttpUploader) throws IOException {
        if (isProgressCancelled && !isUploadComplete(mediaHttpUploader)) {
            driveService.cancelUpload();
            isProgressCancelled = false;
            return;
        }

        log.info("Drive media upload status: " + mediaHttpUploader.getUploadState());
        log.info("Drive media progress: " + mediaHttpUploader.getProgress());
        var progressEvent = new HandleProgressEvent(this);
        progressEvent.setProgress(mediaHttpUploader.getProgress());
        publisher.publishEvent(progressEvent);
    }

    private boolean isUploadComplete(MediaHttpUploader mediaHttpUploader) {
        return mediaHttpUploader.getUploadState() == MediaHttpUploader.UploadState.MEDIA_COMPLETE;
    }

    public void cancelUpload() {
        isProgressCancelled = true;
    }
}
