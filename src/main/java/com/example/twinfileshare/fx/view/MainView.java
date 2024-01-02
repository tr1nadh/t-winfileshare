package com.example.twinfileshare.fx.view;

import com.example.twinfileshare.TWinFileShareApplication;
import com.example.twinfileshare.event.payload.DoubleEmailConnectEvent;
import com.example.twinfileshare.event.payload.HandleProgressEvent;
import com.example.twinfileshare.event.payload.NoDriveAccessEvent;
import com.example.twinfileshare.event.payload.UserConnectedEvent;
import com.example.twinfileshare.fx.MainPresenter;
import com.example.twinfileshare.fx.model.MainModel;
import com.example.twinfileshare.repository.GoogleUserCREDRepository;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Controller
public class MainView {

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
    private ProgressBar mainUploadPB;

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

    public void openFileManager(ActionEvent event) {
        presenter.handleOpenFileManager(event);
    }

    public void removeFile(ActionEvent event) {
        presenter.handleRemoveFiles();
    }

    public void clearListView(ActionEvent event) {
        presenter.handleClearListView();
    }

    public void uploadFiles(ActionEvent event) throws IOException, InterruptedException {
        presenter.handleUploadFiles();
    }

    public void setAccountChoiceBoxItems(List<String> items) {
        accountChoiceBox.getItems().addAll(items);
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

    public ObservableList<String> getSelectedFileListViewItems() {
        return fileListView.getSelectionModel().getSelectedItems();
    }

    public String getAccountChoiceBoxValue() {
        return accountChoiceBox.getValue();
    }

    public void setMainUploadProgressBarVisible(boolean visible) {
        mainUploadPB.setVisible(visible);
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

    public void setMainUploadProgressBarProgress(double value) {
        mainUploadPB.setProgress(value);
    }

    public void setListViewItems(ObservableList<String> items) {
        fileListView.setItems(items);
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

        @Autowired
        private TWinFileShareApplication tWinFileShareApplication;
        @Autowired
        private MainModel mainModel;

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

        private void openAuthLinkInDefaultBrowser() {
            var hostServices = tWinFileShareApplication.getHostServices();
            hostServices.showDocument(mainModel.getGoogleSignInURL());
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
