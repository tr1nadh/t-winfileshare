package com.example.twinfileshare.fx;

import com.example.twinfileshare.fx.controller.MainController;
import com.example.twinfileshare.fx.service.MainService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

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
}
