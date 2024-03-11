package com.example.twinfileshare.event.payload;

import org.springframework.context.ApplicationEvent;

public class UploadCancelledEvent extends ApplicationEvent {

    public UploadCancelledEvent(Object source) {
        super(source);
    }
}
