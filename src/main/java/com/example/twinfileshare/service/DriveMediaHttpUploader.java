package com.example.twinfileshare.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.*;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.util.Sleeper;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.tomcat.util.http.fileupload.IOUtils;

import java.io.IOException;
import java.util.HashMap;

@Log4j2
public class DriveMediaHttpUploader {

    private final AbstractInputStreamContent mediaContent;
    private HashMap<String, String> mediaMetadata;

    @Getter
    private final MediaHttpUploader mediaHttpUploader;

    public DriveMediaHttpUploader(@NonNull AbstractInputStreamContent mediaContent,
                                  @NonNull HttpTransport httpTransport,
                                  @NonNull Credential credential) {
        this.mediaContent = mediaContent;
        this.mediaHttpUploader = new MediaHttpUploader(mediaContent, httpTransport, credential);
    }

    public DriveMediaHttpUploader setDirectUploadEnabled(boolean directUploadEnabled) {
        mediaHttpUploader.setDirectUploadEnabled(directUploadEnabled);
        return this;
    }

    public DriveMediaHttpUploader setSleeper(Sleeper sleeper) {
        mediaHttpUploader.setSleeper(sleeper);
        return this;
    }

    public DriveMediaHttpUploader setMediaMetadata(JsonHttpContent mediaMetadata) {
        var data = mediaMetadata.getData();
        this.mediaMetadata = (HashMap<String, String>) data;

        mediaHttpUploader.setMetadata(mediaMetadata);
        return this;
    }

    private DriveMediaHttpUploaderProgressListener progressListener;

    public DriveMediaHttpUploader setProgressListener(DriveMediaHttpUploaderProgressListener progressListener) {
        progressListener.setDriveMediaHttpUploader(this);
        this.progressListener = progressListener;
        mediaHttpUploader.setProgressListener(progressListener);

        return this;
    }

    public HttpResponse upload(GenericUrl url) {
        var mediaName = mediaMetadata.get("name");
        log.info("Drive media uploading: " + mediaName);

        try {
            return mediaHttpUploader.upload(url);
        } catch (IOException e) {
            throw new IllegalStateException("Drive media uploading stopped: " + mediaName);
        }
    }

    private boolean isUploadCancelled;

    public void cancelUpload() {
        if (!isUploadCancelled) {
            progressListener.cancelProgressUpload();
            isUploadCancelled = true;
            return;
        }

        isUploadCancelled = false;
        if (!isUploadCompleted()) {
            try {
                IOUtils.closeQuietly(mediaContent.getInputStream());
                log.info("Drive media upload cancelled: " + mediaMetadata.get("name"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean isUploadCompleted() {
        return mediaHttpUploader.getUploadState() == MediaHttpUploader.UploadState.MEDIA_COMPLETE;
    }
}
