package com.example.twinfileshare.fx.listener;

import com.example.twinfileshare.TWinFileShareApplication;
import com.example.twinfileshare.event.payload.DoubleEmailConnectEvent;
import com.example.twinfileshare.event.payload.HandleProgressEvent;
import com.example.twinfileshare.event.payload.UnsuccessfulAuthorizationEvent;
import com.example.twinfileshare.event.payload.UserConnectedEvent;
import com.example.twinfileshare.fx.alert.FxAlert;
import com.example.twinfileshare.fx.model.LinkShareModel;
import com.example.twinfileshare.fx.presenter.AccountMangePresenter;
import com.example.twinfileshare.fx.presenter.EmailSharePresenter;
import com.example.twinfileshare.fx.presenter.LinkSharePresenter;
import com.example.twinfileshare.fx.presenter.ManagePresenter;
import com.example.twinfileshare.fx.view.LinkShareView;
import com.example.twinfileshare.repository.GoogleUserCREDRepository;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MainFxListener {

    @Autowired
    private LinkShareView view;

    @Autowired
    private LinkSharePresenter presenter;

    @Autowired
    private FxAlert fxAlert;

    @Autowired
    private EmailSharePresenter emailSharePresenter;

    @EventListener
    public void handleProgressBar(HandleProgressEvent event) {
        Platform.runLater(() -> {
            if (event.getSource() instanceof LinkSharePresenter) {
                presenter.updateProgress(event.getProgress());
            } else if (event.getSource() instanceof EmailSharePresenter) {
                emailSharePresenter.updateProgress(event.getProgress());
            }
        });
    }


    @Autowired
    private ManagePresenter managePresenter;

    @EventListener
    public void handleUserConnectedEvent(UserConnectedEvent event) throws IOException {
        var email = event.getEmail();
        if (!view.accountChoiceBoxContains(email))
            view.addAccountChoiceBoxItem(email);

        Platform.runLater(() -> {
            fxAlert.informationAlert(
                    "Authorization successful",
                    "Google drive successfully connected"
            );
        });

        managePresenter.refresh(email);
        managePresenter.addAccount(email);
    }

    @Autowired
    private TWinFileShareApplication tWinFileShareApplication;
    @Autowired
    private LinkShareModel linkShareModel;

    @Autowired
    private AccountMangePresenter accountMangePresenter;

    @EventListener
    public void handleUnsuccessfulAuthorizationEvent(UnsuccessfulAuthorizationEvent event) {
        Platform.runLater( () -> {
            fxAlert.confirmationAlert(
                    "Authorization unsuccessful",
                    "Google drive and gmail access required!",
                    "Press OK to give access.",
                    accountMangePresenter::openAuthURLInBrowser
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
