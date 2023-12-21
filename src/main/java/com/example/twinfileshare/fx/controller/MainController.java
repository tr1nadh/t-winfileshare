package com.example.twinfileshare.fx.controller;

import com.example.twinfileshare.GoogleUserCREDRepository;
import com.example.twinfileshare.TWinFileShareApplication;
import com.example.twinfileshare.service.GoogleAuthorizationService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
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
    private GoogleUserCREDRepository googleUserCREDRepository;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        accountChoiceBox.getItems().addAll(googleUserCREDRepository.getAllEmails());
    }

    @Autowired
    private GoogleAuthorizationService authorizationService;

    public void disconnectSelectedAccount() throws GeneralSecurityException, IOException {
        var currentSelectedEmail = accountChoiceBox.getValue();
        authorizationService.revokeUserWithEmail(currentSelectedEmail);
        googleUserCREDRepository.deleteByEmail(currentSelectedEmail);
        removeItemFromChoiceBox(currentSelectedEmail);
    }

    private void removeItemFromChoiceBox(String currentSelectedEmail) {
        accountChoiceBox.setValue("Select an email");
        accountChoiceBox.getItems().remove(currentSelectedEmail);
    }
}
