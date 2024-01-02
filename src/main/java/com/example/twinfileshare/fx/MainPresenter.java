package com.example.twinfileshare.fx;

import com.example.twinfileshare.fx.view.MainView;
import com.example.twinfileshare.fx.model.MainModel;
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
    private MainView view;
    @Autowired
    private MainModel model;
    @Autowired
    private FxAlert fxAlert;

    @PostConstruct
    public void init() {
        view.setMainPresenter(this);
        view.setAccountChoiceBoxItems(model.getAllEmails());
        view.changeFileListViewSelectToMultiple();
        view.setMainUploadProgressBarVisible(false);
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
        view.openURLInDefaultBrowser(model.getGoogleSignInURL());
    }

    public void handleDisconnectSelectedAccount() {
        var currentSelectedEmail = view.getCurrentSelectedEmail();
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
        model.disconnectAccount(email);
        view.setAccountChoiceBoxValue("Select an email");
        view.removeEmailFromAccountChoiceBox(email);
    }

    private List<File> totalAddedFiles = new ArrayList<>();

    public void handleOpenFileManager(ActionEvent event) {
        var selectedFiles = view.openMultipleFileChooserWindow(
                "Select files to upload", event);

        if (selectedFiles != null) {
            view.addFileNamesToFileListView(
                    (List<String>) selectedFiles.stream().map(File::getName));
            totalAddedFiles.addAll(selectedFiles);
        }
    }

    public void handleRemoveFiles() {
        var listViewItems = view.getFileListViewItems();
        if (listViewItems.isEmpty()) {
            fxAlert.informationAlert(
                    "Cannot remove",
                    "Add some files to remove"
            );
            return;
        }

        var selectedListViewItems = view.getSelectedFileListViewItems();
        listViewItems.removeAll(selectedListViewItems);
    }

    private boolean isUploadingActive;

    public void handleUploadFiles() throws IOException, InterruptedException {
        if (!view.getAccountChoiceBoxValue().contains("@")) {
            fxAlert.informationAlert(
                    "No email selected",
                    "Select an email to upload files"
            );
            return;
        }
        if (view.getFileListViewItems().isEmpty()) {
            fxAlert.informationAlert(
                    "No files to upload",
                    "Add some files to upload"
            );
            return;
        }

        if (isUploadingActive) {
            System.out.println("Upload cancelled!");
            view.setUploadBTNText("Upload files");
            model.cancelUploadFiles();
            return;
        }

        disableRequiredUploadElements();
        view.setMainUploadProgressBarVisible(true);
        isUploadingActive = true;
        view.setUploadBTNText("Cancel");
        System.out.println("Uploading.........");

        var uploadTask = model.uploadFilesToGoogleDrive(
                view.getAccountChoiceBoxValue(),
                totalAddedFiles,
                view.getFileListViewItems()
        );

        uploadTask.thenAcceptAsync(isFinished -> {
            Platform.runLater(() -> {
                view.setMainUploadProgressBarVisible(false);
                view.setMainUploadProgressBarProgress(0.0);
            });
            isUploadingActive = false;
            Platform.runLater(() -> view.setUploadBTNText("Upload files"));
            if (!isFinished) {
                Platform.runLater(this::showUploadCancelledAlert);
                return;
            }
            System.out.println("Upload finished...");
            totalAddedFiles = new ArrayList<>();
            Platform.runLater(() -> view.getFileListViewItems().clear());
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
        view.disableAccountChoiceBox(true);
        view.disableAccountDisconnectBTN(true);
        view.disableAddFilesBTN(true);
        view.disableRemoveFilesBTN(true);
        view.disableClearFilesBTN(true);
        view.disableFilesListView(true);
    }

    private void enableRequiredUploadElements() {
        view.disableAccountChoiceBox(false);
        view.disableAccountDisconnectBTN(false);
        view.disableAddFilesBTN(false);
        view.disableRemoveFilesBTN(false);
        view.disableClearFilesBTN(false);
        view.disableFilesListView(false);
    }

    public void handleClearListView() {
        if (view.getFileListViewItems().isEmpty()) {
            fxAlert.informationAlert(
                    "No files to clear",
                    "Add some files to clear"
            );
            return;
        }

        view.setListViewItems(FXCollections.observableArrayList());
        totalAddedFiles = new ArrayList<>();
    }
}
