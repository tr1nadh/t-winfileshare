package com.example.twinfileshare.fx.view;

import com.example.twinfileshare.TWinFileShareApplication;
import com.example.twinfileshare.fx.MainPresenter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.ResourceBundle;

@Controller
public class MainView implements Initializable {

    private MainPresenter presenter;

    @FXML
    private Button accountDisconnectBTN;
    @FXML
    private Button addFilesBTN;
    @FXML
    private Button removeFilesBTN;
    @FXML
    private Button clearFilesBTN;
    @FXML
    private ChoiceBox<String> accountChoiceBox;
    @FXML
    private ListView<String> fileListView;
    @FXML
    private Button uploadBTN;
    @FXML
    private ProgressBar fileUploadProgressBar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        presenter.init();
    }

    @Autowired
    private TWinFileShareApplication tWinFileShareApplication;

    public void connectGoogleDrive(ActionEvent event) {
        presenter.handleConnectGoogleDrive();
    }

    public void openURLInDefaultBrowser(String url) {
        var hostServices = tWinFileShareApplication.getHostServices();
        hostServices.showDocument(url);
    }

    public void disconnectSelectedAccount(ActionEvent event) {
        presenter.handleDisconnectSelectedAccount();
    }

    public void addFilesFilesFromFileManager(ActionEvent event) {
        presenter.handleAddFilesFilesFromFileManager(event);
    }

    public void removeFilesFromListView(ActionEvent event) {
        presenter.handleRemoveFilesFromListView();
    }

    public void clearListView(ActionEvent event) {
        presenter.handleClearListView();
    }

    public void uploadFiles(ActionEvent event) throws IOException, InterruptedException, GeneralSecurityException {
        presenter.handleUploadFiles();
    }

    public void setAccountChoiceBoxItems(List<String> items) {
        accountChoiceBox.setItems(FXCollections.observableArrayList(items));
    }

    public void changeFileListViewSelectToMultiple() {
        fileListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void setAccountChoiceBoxValue(String value) {
        accountChoiceBox.setValue(value);
    }

    public void removeEmailFromAccountChoiceBox(String email) {
        accountChoiceBox.getItems().remove(email);
    }

    public List<File> openMultipleFileChooserWindow(String windowTitle, ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(windowTitle);

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        return fileChooser.showOpenMultipleDialog(stage);
    }

    public void addFileNamesToFileListView(List<String> files) {
        fileListView.getItems().addAll(files);
    }

    public ObservableList<String> getFileListViewItems() {
        return fileListView.getItems();
    }

    public void clearFileListViewItems() {
        fileListView.getItems().clear();
    }

    public ObservableList<String> getSelectedFileListViewItems() {
        return fileListView.getSelectionModel().getSelectedItems();
    }

    public String getAccountChoiceBoxValue() {
        return accountChoiceBox.getValue();
    }

    public ObservableList<String> getAccountChoiceBoxItems() {
        return accountChoiceBox.getItems();
    }

    public void setFileUploadProgressBarVisible(boolean visible) {
        fileUploadProgressBar.setVisible(visible);
    }

    public void setUploadBTNText(String text) {
        uploadBTN.setText(text);
    }

    public void disableAccountChoiceBox(boolean value) {
        accountChoiceBox.setDisable(value);
    }

    public void disableAccountDisconnectBTN(boolean value){
        accountDisconnectBTN.setDisable(value);
    }

    public void disableAddFilesBTN(boolean value) {
        addFilesBTN.setDisable(value);
    }

    public void disableRemoveFilesBTN(boolean value) {
        removeFilesBTN.setDisable(value);
    }

    public void disableClearFilesBTN(boolean value) {
        clearFilesBTN.setDisable(value);
    }

    public void disableFilesListView(boolean value) {
        fileListView.setDisable(value);
    }

    public void updateFileUploadProgressBar(double value) {
        fileUploadProgressBar.setProgress(value);
    }

    public double getFileUploadProgressBar() {
        return fileUploadProgressBar.getProgress();
    }

    public void setFileListViewItems(ObservableList<String> items) {
        fileListView.setItems(items);
    }

    public void setMainPresenter(MainPresenter presenter) {
        this.presenter = presenter;
    }

    public String showTextInputDialog(String placeholderText,
                                      String title,
                                      String header) {
        var text = new TextInputDialog();
        text.setTitle(title);
        text.setHeaderText(header);
        text.getEditor().setPromptText(placeholderText);

        return text.showAndWait().orElse(null);
    }
}
