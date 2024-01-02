package com.example.twinfileshare.fx.controller;

import com.example.twinfileshare.TWinFileShareApplication;
import com.example.twinfileshare.event.payload.DoubleEmailConnectEvent;
import com.example.twinfileshare.event.payload.HandleProgressEvent;
import com.example.twinfileshare.event.payload.NoDriveAccessEvent;
import com.example.twinfileshare.event.payload.UserConnectedEvent;
import com.example.twinfileshare.fx.MainPresenter;
import com.example.twinfileshare.fx.service.MainService;
import com.example.twinfileshare.repository.GoogleUserCREDRepository;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Controller
public class MainController implements Initializable {

    @Autowired
    private TWinFileShareApplication tWinFileShareApplication;
    private MainPresenter presenter;
    @Autowired
    private MainService mainService;

    public void connectGoogleDrive(ActionEvent event) {
        presenter.handleConnectGoogleDrive();
    }

    public void openAuthLinkInDefaultBrowser() {
        var hostServices = tWinFileShareApplication.getHostServices();
        hostServices.showDocument(mainService.getGoogleSignInURL());
    }

    @FXML
    private ChoiceBox<String> accountChoiceBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        accountChoiceBox.getItems().addAll(mainService.getAllEmails());
        listViewFiles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        mainUploadPB.setVisible(false);
    }

    public String getCurrentSelectedEmail() {
        return accountChoiceBox.getValue();
    }

    public void disconnectSelectedAccount(ActionEvent event) {
        presenter.handleDisconnectSelectedAccount();
    }

    public void setAccountChoiceBoxValue(String value) {
        accountChoiceBox.setValue(value);
    }

    public void removeEmailFromAccountChoiceBox(String email) {
        accountChoiceBox.getItems().remove(email);
    }

    @FXML
    private ListView<String> listViewFiles;
    private List<File> addedFilesToList;

    public void openFileManager(ActionEvent event) {
        presenter.handleOpenFileManager(event);
    }

    public List<File> openMultipleFileChooserWindow(String windowTitle, ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(windowTitle);

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        return fileChooser.showOpenMultipleDialog(stage);
    }

    public void showFilesInListView(List<File> files) {
        ObservableList<String> fileList = FXCollections.observableArrayList();
        for (var file : files) {
            if (file != null) {
                fileList.add(file.getName());
            }
        }
        listViewFiles.setItems(fileList);
    }

    public ObservableList<String> getListViewItems() {
        return listViewFiles.getItems();
    }

    public ObservableList<String> getSelectedListViewItems() {
        return listViewFiles.getSelectionModel().getSelectedItems();
    }

    public void removeFile(ActionEvent event) {
        presenter.handleRemoveFiles();
    }

    @FXML
    private Button uploadBTN;
    private boolean isUploadingActive;

    @FXML
    private ProgressBar mainUploadPB;

    public String getAccountChoiceBoxValue() {
        return accountChoiceBox.getValue();
    }

    public void uploadFiles(ActionEvent event) throws IOException, InterruptedException {
        presenter.handleUploadFiles();
    }

    public void setMainUploadProgressBarVisible(boolean visible) {
        mainUploadPB.setVisible(visible);
    }

    public void setUploadBTNText(String text) {
        uploadBTN.setText(text);
    }

    @FXML
    private Button accountDisconnectBTN;
    @FXML
    private Button addFilesBTN;
    @FXML
    private Button removeFilesBTN;
    @FXML
    private Button clearFilesBTN;

    public void disableRequiredUploadElements() {
        accountChoiceBox.setDisable(true);
        accountDisconnectBTN.setDisable(true);
        addFilesBTN.setDisable(true);
        removeFilesBTN.setDisable(true);
        clearFilesBTN.setDisable(true);
        listViewFiles.setDisable(true);
    }

    public void enableRequiredUploadElements() {
        accountChoiceBox.setDisable(false);
        accountDisconnectBTN.setDisable(false);
        addFilesBTN.setDisable(false);
        removeFilesBTN.setDisable(false);
        clearFilesBTN.setDisable(false);
        listViewFiles.setDisable(false);
    }

    public void showUploadCancelledAlert() {
        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setResizable(false);
        alert.setTitle("Upload cancelled");
        alert.setHeaderText("Upload has been cancelled!");
        alert.showAndWait();
    }

    public void setMainUploadProgressBarProgress(double value) {
        mainUploadPB.setProgress(value);
    }

    public void showUploadFinishedAlert() {
        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setResizable(false);
        alert.setTitle("Upload success");
        alert.setHeaderText("Successfully files are uploaded!");
        alert.showAndWait();
    }

    public void clearListView(ActionEvent event) {
        if (listViewFiles.getItems().isEmpty()) {
            var alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setResizable(false);
            alert.setTitle("No files to clear");
            alert.setHeaderText("Add some files to clear");
            alert.show();
            return;
        }

        listViewFiles.setItems(FXCollections.observableArrayList());
        addedFilesToList = new ArrayList<>();
    }

    public void setMainPresenter(MainPresenter presenter) {
        this.presenter = presenter;
    }

    @Component
    public class UserConnectedListener {

        @EventListener
        public void handleProgressBar(HandleProgressEvent event) {
            Platform.runLater(() -> {
                if (event.shouldIncrease())
                    mainUploadPB.setProgress(mainUploadPB.getProgress() + 0.1);

                if (event.isComplete()) mainUploadPB.setProgress(1.0);
            });
        }

        @EventListener
        public void handleUserConnectedEvent(UserConnectedEvent event) {
            var email = event.getEmail();
            if (!accountChoiceBox.getItems().contains(email))
                accountChoiceBox.getItems().add(email);

            Platform.runLater(() -> {
                var successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Authorization successful");
                successAlert.setResizable(false);
                successAlert.setHeaderText("Google drive successfully connected");
                successAlert.showAndWait();
            });
        }

        @EventListener
        public void handleNoDriveAccessEvent(NoDriveAccessEvent event) {
            Platform.runLater( () -> {
                var noDriveAccessAlert = new Alert(Alert.AlertType.WARNING);
                noDriveAccessAlert.setTitle("Authorization unsuccessful");
                noDriveAccessAlert.setResizable(false);
                noDriveAccessAlert.setHeaderText("Google drive access required!");
                noDriveAccessAlert.setContentText("Press OK to give google drive access.");
                noDriveAccessAlert.getButtonTypes().add(ButtonType.CANCEL);
                noDriveAccessAlert.showAndWait()
                        .filter(res -> res == ButtonType.OK)
                        .ifPresent(res -> openAuthLinkInDefaultBrowser());
            });
        }

        @Autowired
        private GoogleUserCREDRepository googleUserCREDRepository;

        @EventListener
        public void doubleEmailConnectListener(DoubleEmailConnectEvent event) {
            Platform.runLater(
                    () -> {
                        var doubleEmailAlert = new Alert(Alert.AlertType.ERROR);
                        doubleEmailAlert.setTitle("Double email");
                        doubleEmailAlert.setResizable(false);
                        doubleEmailAlert.setHeaderText(event.getMessage());
                        doubleEmailAlert.setContentText("Do you want to change it to current email: "
                                + event.getCurrentEmail());
                        doubleEmailAlert.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
                        doubleEmailAlert.showAndWait()
                                .filter(res -> res == ButtonType.YES)
                                .ifPresent(res -> {
                                    var dbAccount = event.getGoogleUserCRED();
                                    dbAccount.setEmail(event.getCurrentEmail());
                                    googleUserCREDRepository.save(dbAccount);
                                });
                    }
            );
        }
    }
}
