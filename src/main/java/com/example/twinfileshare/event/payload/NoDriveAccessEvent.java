package com.example.twinfileshare.event.payload;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class NoDriveAccessEvent extends ApplicationEvent {

    public NoDriveAccessEvent(Object source) {
        super(source);
    }
}
