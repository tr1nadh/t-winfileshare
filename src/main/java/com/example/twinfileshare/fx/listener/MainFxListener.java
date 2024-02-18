package com.example.twinfileshare.fx.listener;

import com.example.twinfileshare.TWinFileShareApplication;
import com.example.twinfileshare.event.payload.DoubleEmailConnectEvent;
import com.example.twinfileshare.event.payload.HandleProgressEvent;
import com.example.twinfileshare.event.payload.NoDriveAccessEvent;
import com.example.twinfileshare.event.payload.UserConnectedEvent;
import com.example.twinfileshare.fx.presenter.LinkSharePresenter;
import com.example.twinfileshare.fx.alert.FxAlert;
import com.example.twinfileshare.fx.model.LinkShareModel;
import com.example.twinfileshare.fx.view.LinkShareView;
import com.example.twinfileshare.repository.GoogleUserCREDRepository;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MainFxListener {

    @Autowired
    private LinkShareView view;

    @Autowired
    private LinkSharePresenter presenter;

    @Autowired
    private FxAlert fxAlert;

    @EventListener
    public void handleProgressBar(HandleProgressEvent event) {
        Platform.runLater(() -> {
            presenter.updateProgressBar(event.getProgress());
        });
    }

    @EventListener
    public void handleUserConnectedEvent(UserConnectedEvent event) {
        var email = event.getEmail();
        if (!view.accountChoiceBoxContains(email))
            view.addAccountChoiceBoxItem(email);

        Platform.runLater(() -> {
            fxAlert.informationAlert(
                    "Authorization successful",
                    "Google drive successfully connected"
            );
        });
    }

    @Autowired
    private TWinFileShareApplication tWinFileShareApplication;
    @Autowired
    private LinkShareModel linkShareModel;

    @EventListener
    public void handleNoDriveAccessEvent(NoDriveAccessEvent event) {
        Platform.runLater( () -> {
            fxAlert.confirmationAlert(
                    "Authorization unsuccessful",
                    "Google drive access required!",
                    "Press OK to give google drive access.",
                    presenter::openAuthLinkInDefaultBrowser
            );
        });
    }

    @Autowired
    private GoogleUserCREDRepository googleUserCREDRepository;

    @EventListener
    public void doubleEmailConnectListener(DoubleEmailConnectEvent event) {
        Platform.runLater(() -> showDoubleEmailConfirmationAlert(event));
    }

    private void showDoubleEmailConfirmationAlert(DoubleEmailConnectEvent event) {
        fxAlert.confirmationAlert(
                "Double email",
                event.getMessage(),
                "Do you want to change it to current email: "
                        + event.getCurrentEmail(),
                () -> changeAndSaveEmail(event),
                ButtonType.YES,
                ButtonType.YES, ButtonType.NO
        );
    }

    private void changeAndSaveEmail(DoubleEmailConnectEvent event) {
        var dbAccount = event.getGoogleUserCRED();
        dbAccount.setEmail(event.getCurrentEmail());
        googleUserCREDRepository.save(dbAccount);
    }
}
