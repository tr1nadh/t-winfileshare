package com.example.twinfileshare.event.payload;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SelectedAccountChanged extends ApplicationEvent {

    private String selectedValue;
    public SelectedAccountChanged(Object source, String selectedValue) {
        super(source);
        this.selectedValue = selectedValue;
    }
}
