package com.example.twinfileshare.fx.controller;

import com.example.twinfileshare.GoogleUserCREDJPA;
import com.example.twinfileshare.TWinFileShareApplication;
import com.example.twinfileshare.service.GoogleAuthorizationService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.ResourceBundle;

@Controller
public class MainController implements Initializable {

    @Autowired
    private TWinFileShareApplication tWinFileShareApplication;

    @Autowired
    private GoogleAuthorizationService googleAuthorizationService;

    public void connectGoogleDrive(ActionEvent event) {
        var hostServices = tWinFileShareApplication.getHostServices();
        hostServices.showDocument(googleAuthorizationService.getGoogleSignInURL());
    }

    @FXML
    private ChoiceBox<String> accountChoiceBox;

    @Autowired
    private GoogleUserCREDJPA googleUserCREDJPA;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        accountChoiceBox.getItems().addAll(googleUserCREDJPA.findAllEmail());
    }
}
