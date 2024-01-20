package com.example.twinfileshare.service;

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;

import java.io.IOException;

public abstract class DriveMediaHttpUploaderProgressListener implements MediaHttpUploaderProgressListener {

    private DriveMediaHttpUploader driveMediaHttpUploader;

    private boolean isUploadCancelled;

    @Override
    public final void progressChanged(MediaHttpUploader mediaHttpUploader) throws IOException {
        if (isUploadCancelled) {
            driveMediaHttpUploader.cancelUpload();
            isUploadCancelled = false;
            return;
        }

        uploadProgressChanged(mediaHttpUploader);
    }

    public abstract void uploadProgressChanged(MediaHttpUploader mediaHttpUploader) throws IOException;

    public final void cancelProgressUpload() {
        isUploadCancelled = true;
    }

    public final void setDriveMediaHttpUploader(DriveMediaHttpUploader driveMediaHttpUploader) {
        this.driveMediaHttpUploader = driveMediaHttpUploader;
    }
}
