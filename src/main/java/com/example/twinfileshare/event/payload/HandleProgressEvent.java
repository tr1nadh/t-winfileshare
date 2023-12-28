package com.example.twinfileshare.event.payload;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class HandleProgressEvent extends ApplicationEvent {

    private final boolean increase;
    private final boolean reset;
    private final double increaseValue;

    public HandleProgressEvent(Object source, boolean increase, double increaseValue,
                               boolean reset) {
        super(source);
        this.increase = increase;
        this.increaseValue = increaseValue;
        this.reset = reset;
    }
}
