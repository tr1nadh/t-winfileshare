package com.example.twinfileshare.event.payload;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
public class HandleProgressEvent extends ApplicationEvent {

    private Object source;
    private boolean start;
    private boolean complete;
    private int totalRotations;
    private int currentRotation;
    private boolean shouldIncrease;
    private int tillProgressHappened;
    private int totalRawRotations;

    private static HandleProgressEvent handleProgressEvent;

    public HandleProgressEvent(Object source) {
        super(source);
    }

    @Getter @Setter
    private double progress;

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
        this.complete = false;

        return handleProgressEvent;
    }

    public HandleProgressEvent setComplete(boolean reset) {
        this.complete = reset;

        return handleProgressEvent;
    }

    public HandleProgressEvent setTotalRotations(int totalRotations) {
        this.totalRotations = (totalRotations / 10);
        this.totalRawRotations = totalRotations;

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
        if (isThisPreLastRotation()) completeProgress();

        if (isStart() && !isComplete()) {
            tillProgressHappened += 1;
            if (getCurrentRotation() > 0) {
                setCurrentRotation(getCurrentRotation() - 1);
                return handleProgressEvent;
            }

            shouldIncrease = true;
            setCurrentRotation(getTotalRotations());
        }

        return handleProgressEvent;
    }

    private boolean isThisPreLastRotation() {
        return tillProgressHappened == totalRawRotations - 2;
    }

    public HandleProgressEvent completeProgress() {
        this.complete = true;
        this.start = false;
        tillProgressHappened = 0;

        return handleProgressEvent;
    }

    public boolean shouldIncrease() {
        var actualIncrease = shouldIncrease;
        if (shouldIncrease) {
            shouldIncrease = false;
        }

        return actualIncrease;
    }
}
