package com.example.twinfileshare.event.payload;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class HandleProgressEvent extends ApplicationEvent {

    private double progress;

    public HandleProgressEvent(Object source) {
        super(source);
    }

}
