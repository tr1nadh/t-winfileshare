package com.example.twinfileshare.service;

import com.example.twinfileshare.exception.DriveMediaUploadCancelledException;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.*;
import com.google.api.client.util.Sleeper;
import lombok.NonNull;
import org.apache.tomcat.util.http.fileupload.IOUtils;

import java.io.IOException;

public class DriveMediaHttpUploader {

    private AbstractInputStreamContent mediaContent;
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

    public DriveMediaHttpUploader setMediaMetadata(HttpContent mediaMetadata) {
        mediaHttpUploader.setMetadata(mediaMetadata);
        return this;
    }

    public DriveMediaHttpUploader setProgressListener(MediaHttpUploaderProgressListener progressListener) {
        mediaHttpUploader.setProgressListener(progressListener);
        return this;
    }

    public HttpResponse upload(GenericUrl url) throws IOException {
        return mediaHttpUploader.upload(url);
    }

    public DriveMediaHttpUploader cancelUpload() throws IOException {
        if (!isUploadCompleted()){
            IOUtils.closeQuietly(mediaContent.getInputStream());
            System.out.println("Media '" + mediaHttpUploader.getMetadata().toString() + "'");
        }

        return this;
    }

    private boolean isUploadCompleted() {
        return mediaHttpUploader.getUploadState() == MediaHttpUploader.UploadState.MEDIA_COMPLETE;
    }
}
