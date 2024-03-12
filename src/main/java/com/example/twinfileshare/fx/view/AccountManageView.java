package com.example.twinfileshare.fx.view;

import com.example.twinfileshare.fx.presenter.AccountMangePresenter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Controller
public class AccountManageView implements Initializable {

    @FXML
    private ChoiceBox<String> accountChoiceBox;
    private AccountMangePresenter accountMangePresenter;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        accountMangePresenter.init();
    }

    public void setAccountManagePresenter(AccountMangePresenter accountManagePresenter) {
        this.accountMangePresenter = accountManagePresenter;
    }

    public void disconnectSelectedAccount(ActionEvent event) {
        accountMangePresenter.handleDisconnectSelectedAccount(event);
    }

    public void setItemsToAccountChoiceBox(List<String> items) {
        accountChoiceBox.getItems().setAll(items);
    }

    public void setAccountChoiceBoxValue(String value) {
        accountChoiceBox.setValue(value);
    }

    public void removeItemFromAccountChoiceBox(String item) {
        accountChoiceBox.getItems().remove(item);
    }

    public void openGoogleAuthInBrowser(ActionEvent event) {
        accountMangePresenter.handleOpenGoogleAuthInBrowser();
    }

    public String getAccountChoiceBoxValue() {
        return accountChoiceBox.getValue();
    }

    public void addItemToAccountChoiceBox(String item) {
        accountChoiceBox.getItems().add(item);
    }
}
