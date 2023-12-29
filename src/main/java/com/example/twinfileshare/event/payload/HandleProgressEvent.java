package com.example.twinfileshare.event.payload;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class HandleProgressEvent extends ApplicationEvent {

    private Object source;
    private boolean start;
    private boolean complete;
    private int totalRotations;
    private int currentRotation;
    private boolean shouldIncrease;

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

    public HandleProgressEvent start() {
        this.start = true;

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

    public HandleProgressEvent increaseProgress() {
        if (isStart() && !isComplete()) {
            if (getCurrentRotation() > 0) {
                setCurrentRotation(getCurrentRotation() - 1);
                return handleProgressEvent;
            }

            shouldIncrease = true;
            setCurrentRotation(getTotalRotations());
        }

        return handleProgressEvent;
    }

    public HandleProgressEvent progressCompleted() {
        this.complete = true;

        return handleProgressEvent;
    }

    public boolean shouldIncrease() {
        var actualIncrease = shouldIncrease;
        if (shouldIncrease) shouldIncrease = false;

        return actualIncrease;
    }
}
