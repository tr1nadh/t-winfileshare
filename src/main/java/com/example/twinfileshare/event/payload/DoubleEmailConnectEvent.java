package com.example.twinfileshare.event.payload;

import com.example.twinfileshare.entity.GoogleUserCRED;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class DoubleEmailConnectEvent extends ApplicationEvent {

    private final GoogleUserCRED googleUserCRED;

    private final String message;

    private final String currentEmail;

    public DoubleEmailConnectEvent(Object source, GoogleUserCRED googleUserCRED, String message,
                                   String currentEmail) {
        super(source);
        this.googleUserCRED = googleUserCRED;
        this.message = message;
        this.currentEmail = currentEmail;
    }
}
