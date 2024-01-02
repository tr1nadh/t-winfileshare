package com.example.twinfileshare.fx;

import com.example.twinfileshare.fx.controller.MainController;
import com.example.twinfileshare.fx.service.MainService;
import jakarta.annotation.PostConstruct;
import javafx.scene.control.ButtonType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Component
public class MainPresenter {

    @Autowired
    private MainController controller;
    @Autowired
    private MainService service;
    @Autowired
    private FxAlert fxAlert;

    @PostConstruct
    public void init() {
        controller.setMainPresenter(this);
    }

    public void handleConnectGoogleDrive() {
        fxAlert.confirmationAlert(
                "Google drive authorization",
                "Check the google drive box when giving permissions \n" +
                        " in consent screen to give access.",
                "Press OK to open the link in default browser.",
                controller::openAuthLinkInDefaultBrowser
                );
    }

    public void handleDisconnectSelectedAccount() {
        var currentSelectedEmail = controller.getCurrentSelectedEmail();
        if (!currentSelectedEmail.contains("@")) {
            fxAlert.informationAlert("Cannot disconnect account",
                    "Select an email");
            return;
        }

        fxAlert.confirmationAlert(
                "Disconnect Account",
                "Are sure you want to disconnect the google drive \n" +
                        "account with email: " + currentSelectedEmail,
                "",
                () -> {
                    try {
                        controller.disconnectAccount(currentSelectedEmail);
                    } catch (GeneralSecurityException | IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                ButtonType.YES,
                ButtonType.YES, ButtonType.NO
        );
    }
}
