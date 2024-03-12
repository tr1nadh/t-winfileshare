package com.example.twinfileshare.fx.view;

import com.example.twinfileshare.fx.presenter.EmailSharePresenter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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
    private ListView<File> fileListView;

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

    public void sendAndShare(ActionEvent event) throws IOException {
        emailSharePresenter.handleSendAndShare(event);
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

    public List<File> openMultipleFileChooserWindow(String windowTitle, ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(windowTitle);

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        return fileChooser.showOpenMultipleDialog(stage);
    }

    public void addItemsToFileListView(List<File> items) {
        fileListView.getItems().addAll(items);
    }

    public List<File> getFileListViewItems() {
        return fileListView.getItems();
    }

    public List<File> getSelectedFileListViewItems() {
        return fileListView.getSelectionModel().getSelectedItems();
    }

    public boolean isFileListViewEmpty() {
        return fileListView.getItems().isEmpty();
    }

    public void removeAllFileListViewItems() {
        fileListView.getItems().setAll(new ArrayList<>());
    }

    public void addFilesFilesFromFileManager(ActionEvent event) {
        emailSharePresenter.handleAddFilesFilesFromFileManager(event);
    }

    public void removeFilesFromListView(ActionEvent event) {
        emailSharePresenter.handleRemoveFilesFromListView();
    }

    public void clearListView(ActionEvent event) {
        emailSharePresenter.handleClearListView();
    }

    public String getAccountChoiceBoxValue() {
        return fromAccountChoiceBox.getValue();
    }

    public String showTextInputDialog(String zipName, String header, String placeholderText) {
        var text = new TextInputDialog();
        text.setTitle(zipName);
        text.setHeaderText(header);
        text.getEditor().setPromptText(placeholderText);

        return text.showAndWait().orElse(null);
    }

    public void addItemToFromAccountChoiceBox(String item) {
        fromAccountChoiceBox.getItems().add(item);
    }
}
