package com.example.twinfileshare.fx.view;

import com.example.twinfileshare.fx.presenter.UploadPresenter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.ResourceBundle;

@Controller
public class UploadView implements IUploadView {

    private UploadPresenter presenter;

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

    public void setAccountChoiceBoxItems(List<String> items) {
        accountChoiceBox.setItems(FXCollections.observableArrayList(items));
    }

    @Override
    public void addAccountChoiceBoxItem(String item) {
        accountChoiceBox.getItems().add(item);
    }

    public void setAccountChoiceBoxValue(String value) {
        accountChoiceBox.setValue(value);
    }

    public void removeItemFromAccountChoiceBox(String item) {
        accountChoiceBox.getItems().remove(item);
    }

    public String getAccountChoiceBoxValue() {
        return accountChoiceBox.getValue();
    }

    public void disableAccountChoiceBox(boolean disable) {
        accountChoiceBox.setDisable(disable);
    }

    public boolean accountChoiceBoxContains(String item) {
        return accountChoiceBox.getItems().contains(item);
    }

    public void disableAccountDisconnectBTN(boolean disable){
        accountDisconnectBTN.setDisable(disable);
    }

    public void disableAddFilesBTN(boolean disable) {
        addFilesBTN.setDisable(disable);
    }

    public void disableRemoveFilesBTN(boolean disable) {
        removeFilesBTN.setDisable(disable);
    }

    public void disableClearFilesBTN(boolean disable) {
        clearFilesBTN.setDisable(disable);
    }

    @Override
    public void disableHistoryBTN(boolean disable) {

    }

    public void setFileListViewSelectionMode(SelectionMode selectionMode) {
        fileListView.getSelectionModel().setSelectionMode(selectionMode);
    }

    public void addItemsToFileListView(List<String> files) {
        fileListView.getItems().addAll(files);
    }

    public ObservableList<String> getFileListViewItems() {
        return fileListView.getItems();
    }

    @Override
    public boolean isFileListViewEmpty() {
        return fileListView.getItems().isEmpty();
    }

    public void clearFileListViewItems() {
        fileListView.getItems().clear();
    }

    public ObservableList<String> getSelectedFileListViewItems() {
        return fileListView.getSelectionModel().getSelectedItems();
    }

    public void disableFileListView(boolean disable) {
        fileListView.setDisable(disable);
    }

    public void setFileListViewItems(ObservableList<String> items) {
        fileListView.setItems(items);
    }

    public void setFileUploadProgressBarVisible(boolean visible) {
        fileUploadProgressBar.setVisible(visible);
    }

    public void updateFileUploadProgressBar(double value) {
        fileUploadProgressBar.setProgress(value);
    }

    public void setUploadBTNText(String text) {
        uploadBTN.setText(text);
    }

    public List<File> openMultipleFileChooserWindow(String windowTitle, ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(windowTitle);

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        return fileChooser.showOpenMultipleDialog(stage);
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

    public void setUploadPresenter(UploadPresenter presenter) {
        this.presenter = presenter;
    }

    public void connectGoogleDrive(ActionEvent event) {
        presenter.handleConnectGoogleDrive();
    }

    public void disconnectSelectedAccount(ActionEvent event) {
        presenter.handleDisconnectSelectedAccount();
    }

    public void addFilesFromFileManager(ActionEvent event) {
        presenter.handleAddFilesFilesFromFileManager(event);
    }

    public void removeFilesFromFileListView(ActionEvent event) {
        presenter.handleRemoveFilesFromListView();
    }

    public void clearFileListView(ActionEvent event) {
        presenter.handleClearListView();
    }

    public void changeToHistoryScene(ActionEvent event) throws IOException {
        presenter.handleChangeToHistoryScene();
    }

    public void uploadFiles(ActionEvent event) throws IOException, InterruptedException, GeneralSecurityException {
        presenter.handleUploadFiles();
    }
}
