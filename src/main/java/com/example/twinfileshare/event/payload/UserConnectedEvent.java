package com.example.twinfileshare.event.payload;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserConnectedEvent extends ApplicationEvent {

    private final String email;

    public UserConnectedEvent(Object source, String email) {
        super(source);
        this.email = email;
    }
}
