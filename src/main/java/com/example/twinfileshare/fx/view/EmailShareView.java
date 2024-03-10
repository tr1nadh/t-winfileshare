package com.example.twinfileshare.fx.view;

import com.example.twinfileshare.fx.presenter.EmailSharePresenter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.springframework.stereotype.Controller;

import javax.mail.MessagingException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Controller
public class EmailShareView implements Initializable {

    @FXML
    private ChoiceBox<String> fromAccountChoiceBox;

    @FXML
    private TextArea titleText;

    @FXML
    private TextArea toText;

    @FXML
    private TextArea messageBodyText;

    @FXML
    private Button addFilesBTN;

    @FXML
    private Button removeFilesBTN;

    @FXML
    private Button clearFilesBTN;

    @FXML
    private ListView<String> fileListView;

    @FXML
    private Button sendBTN;

    @FXML
    private ProgressBar fileUploadProgressBar;

    @FXML
    private Button manageAccounts;

    private EmailSharePresenter emailSharePresenter;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        emailSharePresenter.init();
    }

    public void setFromAccountChoiceBoxItems(List<String> items) {
        fromAccountChoiceBox.getItems().addAll(items);
    }

    public void manageAccounts(ActionEvent event) {
        emailSharePresenter.handleManageAccounts(event);
    }

    public void sendAndShare(ActionEvent event) throws MessagingException, IOException {
        emailSharePresenter.handleSendAndShare();
    }

    public void setEmailSharePresenter(EmailSharePresenter emailSharePresenter) {
        this.emailSharePresenter = emailSharePresenter;
    }

    public String getSelectedFromAccountChoiceBoxItem() {
        return fromAccountChoiceBox.getValue();
    }

    public String getToTextValue() {
        return toText.getText();
    }

    public String getTitleTextValue() {
        return titleText.getText();
    }

    public String getMessageBodyTextValue() {
        return messageBodyText.getText();
    }

    public void removeItemFromChoiceBox(String item) {
        fromAccountChoiceBox.getItems().remove(item);
    }
}
