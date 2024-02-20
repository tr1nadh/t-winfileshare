package com.example.twinfileshare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

@Profile({"dev", "prod"})
@SpringBootApplication
public class TwinFileShareSpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(TwinFileShareSpringBootApplication.class, args);
    }
}
