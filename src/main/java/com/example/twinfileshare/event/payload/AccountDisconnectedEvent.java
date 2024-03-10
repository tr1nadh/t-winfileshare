package com.example.twinfileshare.event.payload;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AccountDisconnectedEvent extends ApplicationEvent {

    private final String email;

    public AccountDisconnectedEvent(Object source, String email) {
        super(source);
        this.email = email;
    }
}
