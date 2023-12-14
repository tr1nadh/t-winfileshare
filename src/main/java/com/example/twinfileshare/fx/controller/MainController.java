package com.example.twinfileshare.fx.controller;

import com.example.twinfileshare.TWinFileShareApplication;
import com.example.twinfileshare.fx.service.DriveService;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class MainController {

    @Autowired
    private TWinFileShareApplication tWinFileShareApplication;

    @Autowired
    private DriveService driveService;

    public void connectGoogleDrive(ActionEvent event) {
        var hostServices = tWinFileShareApplication.getHostServices();
        hostServices.showDocument(driveService.getGoogleSignInURL());
    }
}
