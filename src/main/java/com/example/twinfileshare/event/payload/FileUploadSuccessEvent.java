package com.example.twinfileshare.event.payload;

import com.example.twinfileshare.service.DriveUploadResponse;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class FileUploadSuccessEvent extends ApplicationEvent {

    @Getter
    private final DriveUploadResponse driveUploadResponse;

    public FileUploadSuccessEvent(Object source, DriveUploadResponse driveUploadResponse) {
        super(source);
        this.driveUploadResponse = driveUploadResponse;
    }
}
