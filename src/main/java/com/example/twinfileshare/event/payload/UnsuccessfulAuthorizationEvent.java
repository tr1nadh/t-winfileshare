package com.example.twinfileshare.event.payload;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UnsuccessfulAuthorizationEvent extends ApplicationEvent {

    public UnsuccessfulAuthorizationEvent(Object source) {
        super(source);
    }
}
