package com.example.twinfileshare.event.payload;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class HandleProgressEvent extends ApplicationEvent {

    private Object source;
    private boolean increase;
    private boolean complete;
    private int totalRotations;
    private int currentRotation;

    private static HandleProgressEvent handleProgressEvent;

    private HandleProgressEvent(Object source) {
        super(source);
    }

    public static HandleProgressEvent getInstance() {
        if (handleProgressEvent == null)
            handleProgressEvent = new HandleProgressEvent(new Object());

        return handleProgressEvent;
    }

    public HandleProgressEvent setSource(Object source) {
        this.source = source;

        return handleProgressEvent;
    }

    public HandleProgressEvent setIncrease(boolean increase) {
        this.increase = increase;

        return handleProgressEvent;
    }

    public HandleProgressEvent setComplete(boolean reset) {
        this.complete = reset;

        return handleProgressEvent;
    }

    public HandleProgressEvent setTotalRotations(int totalRotations) {
        this.totalRotations = totalRotations;

        return handleProgressEvent;
    }

    public HandleProgressEvent setCurrentRotation(int currentRotation) {
        this.currentRotation = currentRotation;

        return handleProgressEvent;
    }

    public HandleProgressEvent close() {
        return handleProgressEvent;
    }

}
