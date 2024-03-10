package com.example.twinfileshare.fx.presenter;

import com.example.twinfileshare.fx.alert.FxAlert;
import com.example.twinfileshare.fx.model.AccountMangeModal;
import com.example.twinfileshare.fx.view.AccountManageView;
import jakarta.annotation.PostConstruct;
import javafx.event.ActionEvent;
import javafx.scene.control.ButtonType;
import org.controlsfx.control.Notifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
public class AccountMangePresenter {

    @Autowired
    private AccountManageView accountManageView;

    @Autowired
    private AccountMangeModal accountMangeModal;

    public void init() {
        var emails = accountMangeModal.getAllEmails();
        accountManageView.setItemsToAccountChoiceBox(emails);
    }

    @PostConstruct
    private void setAccountManagePresenter() {
        accountManageView.setAccountManagePresenter(this);
    }

    @Autowired
    private FxAlert fxAlert;

    public void handleDisconnectSelectedAccount(ActionEvent event) {
        var currentSelectedEmail = accountManageView.getAccountChoiceBoxValue();
        if (!isEmail(currentSelectedEmail)) {
            Notifications.create()
                    .title("Cannot disconnect account")
                    .text("Select an email")
                    .owner(event.getSource())
                    .showInformation();
            return;
        }

        fxAlert.confirmationAlert(
                "Disconnect Account",
                "Are sure you want to disconnect the google drive \n" +
                        "account with email: " + currentSelectedEmail,
                "",
                () -> {
                    try {
                        disconnectAccount(currentSelectedEmail);
                    } catch (GeneralSecurityException | IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                ButtonType.YES,
                ButtonType.YES, ButtonType.NO
        );
    }

    private void disconnectAccount(String email) throws GeneralSecurityException, IOException {
        accountMangeModal.disconnectAccount(email);
        accountManageView.setAccountChoiceBoxValue("Select an email");
        accountManageView.removeItemFromAccountChoiceBox(email);
    }

    private boolean isEmail(String currentSelectedEmail) {
        return currentSelectedEmail.contains("@");
    }
}
