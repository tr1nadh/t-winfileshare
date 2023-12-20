package com.example.twinfileshare.fx.controller;

import com.example.twinfileshare.TWinFileShareApplication;
import com.example.twinfileshare.service.GoogleAuthorizationService;
import javafx.event.ActionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class MainController {

    @Autowired
    private TWinFileShareApplication tWinFileShareApplication;

    @Autowired
    private GoogleAuthorizationService googleAuthorizationService;

    public void connectGoogleDrive(ActionEvent event) {
        var hostServices = tWinFileShareApplication.getHostServices();
        hostServices.showDocument(googleAuthorizationService.getGoogleSignInURL());
    }
}
