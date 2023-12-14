package com.example.twinfileshare.fx.controller;

import javafx.event.ActionEvent;
import org.springframework.stereotype.Controller;

@Controller
public class MainController {

    public void connectGoogleDrive(ActionEvent event) {
        System.out.println("Connected to google drive");
    }
}
