package com.example.twinfileshare.fx.controller;

import com.example.twinfileshare.TWinFileShareApplication;
import com.example.twinfileshare.event.payload.DoubleEmailConnectEvent;
import com.example.twinfileshare.event.payload.HandleProgressEvent;
import com.example.twinfileshare.event.payload.NoDriveAccessEvent;
import com.example.twinfileshare.event.payload.UserConnectedEvent;
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
import java.util.concurrent.ExecutionException;

@Controller
public class MainController implements Initializable {

    @Autowired
    private TWinFileShareApplication tWinFileShareApplication;

    @Autowired
    private MainService mainService;

    public void connectGoogleDrive(ActionEvent event) {
        var alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setResizable(false);
        alert.setTitle("Google drive authorization");
        alert.setHeaderText("Check the google drive box when giving permissions \n" +
                " in consent screen to give access.");
        alert.setContentText("Press OK to open the link in default browser.");
        alert.showAndWait()
                .filter(res -> res == ButtonType.OK)
                .ifPresent(res -> openAuthLinkInDefaultBrowser());
    }

    private void openAuthLinkInDefaultBrowser() {
        var hostServices = tWinFileShareApplication.getHostServices();
        hostServices.showDocument(mainService.getGoogleSignInURL());
    }

    @FXML
    private ChoiceBox<String> accountChoiceBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        accountChoiceBox.getItems().addAll(mainService.getAllEmails());
        mainUploadPB.setVisible(false);
    }

    public void disconnectSelectedAccount() {
        var currentSelectedEmail = accountChoiceBox.getValue();
        if (!currentSelectedEmail.contains("@")) {
            var alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setResizable(false);
            alert.setTitle("Cannot disconnect account");
            alert.setHeaderText("Select an email");
            alert.show();
            return;
        }

        var alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Disconnect Account");
        alert.setHeaderText("Are sure you want to disconnect the google drive \n" +
                "account with email: " + currentSelectedEmail);
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        alert.showAndWait()
                        .filter(res -> res == ButtonType.YES)
                .ifPresent(res -> {
                    try {
                        disconnectAccount(currentSelectedEmail);
                    } catch (GeneralSecurityException | IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        alert.setResizable(false);

    }

    private void disconnectAccount(String currentSelectedEmail) throws GeneralSecurityException, IOException {
        mainService.disconnectAccount(currentSelectedEmail);
        removeItemFromChoiceBox(currentSelectedEmail);
    }

    private void removeItemFromChoiceBox(String currentSelectedEmail) {
        accountChoiceBox.setValue("Select an email");
        accountChoiceBox.getItems().remove(currentSelectedEmail);
    }

    @FXML
    private ListView<String> listViewFiles;

    private final ObservableList<String> fileList = FXCollections.observableArrayList();

    private List<File> addedFilesToList;

    public void openFileManager(ActionEvent event) {
        listViewFiles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a File");

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        addedFilesToList = fileChooser.showOpenMultipleDialog(stage);

        if (addedFilesToList != null) showInListView();
    }

    private void showInListView() {
        for (var file : addedFilesToList) {
            if (file != null) {
                fileList.add(file.getName());
            }
        }
        listViewFiles.setItems(fileList);
    }

    public void removeFile(ActionEvent event) {
        if (listViewFiles.getItems().isEmpty()) {
            var alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setResizable(false);
            alert.setTitle("Cannot remove");
            alert.setHeaderText("Add some files to remove");
            alert.show();
            return;
        }

        var selectedItems = listViewFiles.getSelectionModel().getSelectedItems();
        listViewFiles.getItems().removeAll(selectedItems);
    }

    @FXML
    private Button uploadBTN;
    private boolean isUploadingActive;

    @FXML
    private ProgressBar mainUploadPB;

    public void uploadFiles(ActionEvent event) throws IOException, InterruptedException {
        if (!accountChoiceBox.getValue().contains("@")) {
            var alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setResizable(false);
            alert.setTitle("No email selected");
            alert.setHeaderText("Select an email to upload files");
            alert.show();
            return;
        }
        if (listViewFiles.getItems().isEmpty()) {
            var alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setResizable(false);
            alert.setTitle("No files to upload");
            alert.setHeaderText("Add some files to upload");
            alert.show();
            return;
        }

        if (isUploadingActive) {
            System.out.println("Upload cancelled!");
            uploadBTN.setText("Upload files");
            mainService.cancelUploadFiles();
            return;
        }

        mainUploadPB.setVisible(true);
        isUploadingActive = true;
        uploadBTN.setText("Cancel");
        System.out.println("Uploading.........");

        var uploadTask = mainService.uploadFilesToGoogleDrive(
                accountChoiceBox.getValue(),
                addedFilesToList,
                listViewFiles.getItems()
        );

        uploadTask.thenAcceptAsync(isFinished -> {
            Platform.runLater(() -> {
                mainUploadPB.setVisible(false);
                mainUploadPB.setProgress(0.0);
            });
            isUploadingActive = false;
            Platform.runLater(() -> uploadBTN.setText("Upload files"));
            if (!isFinished) {
                Platform.runLater(this::showUploadCancelledAlert);
                return;
            }
            System.out.println("Upload finished...");
            addedFilesToList = new ArrayList<>();
            Platform.runLater(() -> listViewFiles.getItems().clear());
            Platform.runLater(this::showUploadFinishedAlert);
        });
    }

    private void showUploadCancelledAlert() {
        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setResizable(false);
        alert.setTitle("Upload cancelled");
        alert.setHeaderText("Upload has been cancelled!");
        alert.showAndWait();
    }

    private void showUploadFinishedAlert() {
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
