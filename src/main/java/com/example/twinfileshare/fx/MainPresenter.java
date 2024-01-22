package com.example.twinfileshare.fx;

import com.example.twinfileshare.fx.alert.FxAlert;
import com.example.twinfileshare.fx.model.MainModel;
import com.example.twinfileshare.fx.view.MainView;
import jakarta.annotation.PostConstruct;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ButtonType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class MainPresenter {

    @Autowired
    private MainView view;
    @Autowired
    private MainModel model;
    @Autowired
    private FxAlert fxAlert;

    @PostConstruct
    public void setView() {
        view.setMainPresenter(this);
    }

    public void init() {
        view.setAccountChoiceBoxItems(model.getAllEmails());
        view.changeFileListViewSelectToMultiple();
        view.setFileUploadProgressBarVisible(false);
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
        var currentSelectedEmail = view.getAccountChoiceBoxValue();
        if (!isEmail(currentSelectedEmail)) {
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

    private boolean isEmail(String currentSelectedEmail) {
        return currentSelectedEmail.contains("@");
    }

    private void disconnectAccount(String email) throws GeneralSecurityException, IOException {
        model.disconnectAccount(email);
        view.setAccountChoiceBoxValue("Select an email");
        view.removeEmailFromAccountChoiceBox(email);
    }

    private List<File> totalAddedFiles = new ArrayList<>();

    public void handleAddFilesFilesFromFileManager(ActionEvent event) {
        var selectedFiles = view.openMultipleFileChooserWindow(
                "Select files to upload", event);

        if (selectedFiles != null) {
            view.addFileNamesToFileListView(
                    selectedFiles.stream().map(File::getName).toList());
            totalAddedFiles.addAll(selectedFiles);
        }
    }

    public void handleRemoveFilesFromListView() {
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

    public void handleUploadFiles() throws IOException, InterruptedException, GeneralSecurityException {
        var selectedEmail = view.getAccountChoiceBoxValue();
        if (!isEmail(selectedEmail)) {
            fxAlert.informationAlert(
                    "No email selected",
                    "Select an email to upload files"
            );
            return;
        }

        var requiredFileNames = view.getFileListViewItems();
        if (requiredFileNames.isEmpty()) {
            fxAlert.informationAlert(
                    "No files to upload",
                    "Add some files to upload"
            );
            return;
        }

        if (isUploadingActive) {
            cancelUpload();
            return;
        }

        String zipName = null;
        if (requiredFileNames.size() > 1) {
            zipName = getZipName();
            if (zipName == null) return;
            if (zipName.isEmpty() || zipName.isBlank()) {
                fxAlert.errorAlert(
                        "Zip name error",
                        "Zip name cannot be empty or blank",
                        ""
                );

                return;
            }

        }

        executePreUploadTasks();

        var requiredFiles = getRequiredFiles(requiredFileNames);
        CompletableFuture<Boolean> uploadTask =
                getUploadTask(selectedEmail, requiredFiles, zipName);

        uploadTask.thenAcceptAsync(isFinished -> {
            executePostUploadTasks();
            if (isFinished) executeUploadFinishedTasks();
            else Platform.runLater(this::showUploadCancelledAlert);
        }).exceptionallyAsync(this::executeUploadTaskException);
    }

    private CompletableFuture<Boolean> getUploadTask(String selectedEmail, List<File> requiredFiles, String zipName) throws GeneralSecurityException, IOException, InterruptedException {
        if (requiredFiles.size() > 1)
            return model.uploadFilesToGoogleDrive(selectedEmail, requiredFiles,
                    zipName);
        else
            return model.uploadFileToGoogleDrive(selectedEmail,
                    requiredFiles.getFirst());
    }

    private ArrayList<File> getRequiredFiles(ObservableList<String> requiredFileNames) {
        var totalFiles = totalAddedFiles;
        var requiredFiles = new ArrayList<File>();
        totalFiles.stream().filter(file -> requiredFileNames.contains(file.getName()))
                .forEach(requiredFiles::add);

        return requiredFiles;
    }

    private Void executeUploadTaskException(Throwable ex) {
        executePostUploadTasks();
        Platform.runLater(() -> {
            showUploadExceptionAlert(ex.getCause().getMessage());
        });
        return null;
    }

    private String getZipName() {
        return view.showTextInputDialog(
                "zip name",
                "Name for zipping...",
                "Enter a name to zip the files"
        );
    }

    private void showUploadExceptionAlert(String message) {
        fxAlert.errorAlert(
                "Upload error",
                "Something went wrong",
                message
        );
    }

    private void executeUploadFinishedTasks() {
        System.out.println("Upload finished...");
        totalAddedFiles = new ArrayList<>();
        Platform.runLater(() -> {
            view.clearFileListViewItems();
            showUploadFinishedAlert();
        });
    }

    private void executePostUploadTasks() {
        isUploadingActive = false;
        Platform.runLater(() -> {
            view.setFileUploadProgressBarVisible(false);
            view.updateFileUploadProgressBar(0.0);
            view.setUploadBTNText("Upload files");
            enableRequiredUIElements();
        });
    }

    private void executePreUploadTasks() {
        disableRequiredUIElements();
        view.setFileUploadProgressBarVisible(true);
        isUploadingActive = true;
        view.setUploadBTNText("Cancel");
        System.out.println("Uploading.........");
    }

    private void cancelUpload() {
        System.out.println("Upload cancelled!");
        view.setUploadBTNText("Upload files");
        model.cancelUploadFiles();
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

    private void disableRequiredUIElements() {
        view.disableAccountChoiceBox(true);
        view.disableAccountDisconnectBTN(true);
        view.disableAddFilesBTN(true);
        view.disableRemoveFilesBTN(true);
        view.disableClearFilesBTN(true);
        view.disableFilesListView(true);
    }

    private void enableRequiredUIElements() {
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

        view.setFileListViewItems(FXCollections.observableArrayList());
        totalAddedFiles = new ArrayList<>();
    }

    public void updateProgressBar(double value) {
        if (isUploadingActive)
            view.updateFileUploadProgressBar(value);
    }
}
