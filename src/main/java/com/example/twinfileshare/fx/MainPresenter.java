package com.example.twinfileshare.fx;

import com.example.twinfileshare.TWinFileShareApplication;
import com.example.twinfileshare.fx.controller.MainController;
import com.example.twinfileshare.fx.service.MainService;
import jakarta.annotation.PostConstruct;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.ButtonType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Component
public class MainPresenter {

    @Autowired
    private MainController controller;
    @Autowired
    private MainService service;
    @Autowired
    private FxAlert fxAlert;

    @PostConstruct
    public void init() {
        controller.setMainPresenter(this);
        controller.setAccountChoiceBoxItems(service.getAllEmails());
        controller.changeFileListViewSelectToMultiple();
        controller.setMainUploadProgressBarVisible(false);
    }

    public void handleConnectGoogleDrive() {
        fxAlert.confirmationAlert(
                "Google drive authorization",
                "Check the google drive box when giving permissions \n" +
                        " in consent screen to give access.",
                "Press OK to open the link in default browser.",
                this::openAuthLinkInDefaultBrowser
                );
    }

    public void openAuthLinkInDefaultBrowser() {
        controller.openURLInDefaultBrowser(service.getGoogleSignInURL());
    }

    public void handleDisconnectSelectedAccount() {
        var currentSelectedEmail = controller.getCurrentSelectedEmail();
        if (!currentSelectedEmail.contains("@")) {
            fxAlert.informationAlert("Cannot disconnect account",
                    "Select an email");
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
        service.disconnectAccount(email);
        controller.setAccountChoiceBoxValue("Select an email");
        controller.removeEmailFromAccountChoiceBox(email);
    }

    private List<File> totalAddedFiles = new ArrayList<>();

    public void handleOpenFileManager(ActionEvent event) {
        var selectedFiles = controller.openMultipleFileChooserWindow(
                "Select files to upload", event);

        if (selectedFiles != null) {
            controller.addFileNamesToFileListView(
                    (List<String>) selectedFiles.stream().map(File::getName));
            totalAddedFiles.addAll(selectedFiles);
        }
    }

    public void handleRemoveFiles() {
        var listViewItems = controller.getFileListViewItems();
        if (listViewItems.isEmpty()) {
            fxAlert.informationAlert(
                    "Cannot remove",
                    "Add some files to remove"
            );
            return;
        }

        var selectedListViewItems = controller.getSelectedFileListViewItems();
        listViewItems.removeAll(selectedListViewItems);
    }

    private boolean isUploadingActive;

    public void handleUploadFiles() throws IOException, InterruptedException {
        if (!controller.getAccountChoiceBoxValue().contains("@")) {
            fxAlert.informationAlert(
                    "No email selected",
                    "Select an email to upload files"
            );
            return;
        }
        if (controller.getFileListViewItems().isEmpty()) {
            fxAlert.informationAlert(
                    "No files to upload",
                    "Add some files to upload"
            );
            return;
        }

        if (isUploadingActive) {
            System.out.println("Upload cancelled!");
            controller.setUploadBTNText("Upload files");
            service.cancelUploadFiles();
            return;
        }

        disableRequiredUploadElements();
        controller.setMainUploadProgressBarVisible(true);
        isUploadingActive = true;
        controller.setUploadBTNText("Cancel");
        System.out.println("Uploading.........");

        var uploadTask = service.uploadFilesToGoogleDrive(
                controller.getAccountChoiceBoxValue(),
                totalAddedFiles,
                controller.getFileListViewItems()
        );

        uploadTask.thenAcceptAsync(isFinished -> {
            Platform.runLater(() -> {
                controller.setMainUploadProgressBarVisible(false);
                controller.setMainUploadProgressBarProgress(0.0);
            });
            isUploadingActive = false;
            Platform.runLater(() -> controller.setUploadBTNText("Upload files"));
            if (!isFinished) {
                Platform.runLater(this::showUploadCancelledAlert);
                return;
            }
            System.out.println("Upload finished...");
            totalAddedFiles = new ArrayList<>();
            Platform.runLater(() -> controller.getFileListViewItems().clear());
            Platform.runLater(this::showUploadFinishedAlert);
        });

        enableRequiredUploadElements();
    }

    private void showUploadCancelledAlert() {
        fxAlert.informationAlert(
                "Upload cancelled",
                "Upload has been cancelled!"
        );
    }

    private void showUploadFinishedAlert() {
        fxAlert.informationAlert(
                "Upload success",
                "Successfully files are uploaded!"
        );
    }

    private void disableRequiredUploadElements() {
        controller.disableAccountChoiceBox(true);
        controller.disableAccountDisconnectBTN(true);
        controller.disableAddFilesBTN(true);
        controller.disableRemoveFilesBTN(true);
        controller.disableClearFilesBTN(true);
        controller.disableFilesListView(true);
    }

    private void enableRequiredUploadElements() {
        controller.disableAccountChoiceBox(false);
        controller.disableAccountDisconnectBTN(false);
        controller.disableAddFilesBTN(false);
        controller.disableRemoveFilesBTN(false);
        controller.disableClearFilesBTN(false);
        controller.disableFilesListView(false);
    }

    public void handleClearListView() {
        if (controller.getFileListViewItems().isEmpty()) {
            fxAlert.informationAlert(
                    "No files to clear",
                    "Add some files to clear"
            );
            return;
        }

        controller.setListViewItems(FXCollections.observableArrayList());
        totalAddedFiles = new ArrayList<>();
    }
}
