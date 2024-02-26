package com.example.twinfileshare;

import com.vaadin.flow.component.page.AppShellConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TWFSSpringBootVaadinApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(TWFSSpringBootVaadinApplication.class, args);
    }
}
