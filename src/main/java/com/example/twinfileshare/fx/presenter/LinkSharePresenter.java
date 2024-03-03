package com.example.twinfileshare.fx.presenter;

import com.example.twinfileshare.TWinFileShareApplication;
import com.example.twinfileshare.event.payload.FileUploadSuccessEvent;
import com.example.twinfileshare.fx.TWFSFxApplication;
import com.example.twinfileshare.fx.alert.FxAlert;
import com.example.twinfileshare.fx.model.LinkShareModel;
import com.example.twinfileshare.fx.view.ILinkShareView;
import com.example.twinfileshare.service.DriveUploadResponse;
import jakarta.annotation.PostConstruct;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class LinkSharePresenter {

    @Autowired
    private ILinkShareView uploadView;
    @Autowired
    private LinkShareModel linkShareModel;
    @Autowired
    private FxAlert fxAlert;

    @PostConstruct
    public void setView() {
        uploadView.setUploadPresenter(this);
    }

    public void init() {
        uploadView.setAccountChoiceBoxItems(linkShareModel.getAllEmails());
        uploadView.setFileListViewSelectionMode(SelectionMode.MULTIPLE);
        uploadView.setFileUploadProgressBarVisible(false);
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

    @Autowired
    private TWinFileShareApplication tWinFileShareApplication;

    public void openAuthLinkInDefaultBrowser() {
        var hostServices = tWinFileShareApplication.getHostServices();
        hostServices.showDocument(linkShareModel.getGoogleSignInURL());
    }

    public void handleDisconnectSelectedAccount() {
        var currentSelectedEmail = uploadView.getAccountChoiceBoxValue();
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
        linkShareModel.disconnectAccount(email);
        uploadView.setAccountChoiceBoxValue("Select an email");
        uploadView.removeItemFromAccountChoiceBox(email);
    }

    private List<File> totalAddedFiles = new ArrayList<>();

    public void handleAddFilesFilesFromFileManager(ActionEvent event) {
        var selectedFiles = uploadView.openMultipleFileChooserWindow(
                "Select files to upload", event);

        if (selectedFiles != null) {
            uploadView.addItemsToFileListView(
                    selectedFiles.stream().map(File::getName).toList());
            totalAddedFiles.addAll(selectedFiles);
        }
    }

    public void handleRemoveFilesFromListView() {
        var listViewItems = uploadView.getFileListViewItems();
        if (listViewItems.isEmpty()) {
            fxAlert.informationAlert(
                    "Cannot remove",
                    "Add some files to remove"
            );
            return;
        }

        var selectedListViewItems = uploadView.getSelectedFileListViewItems();
        listViewItems.removeAll(selectedListViewItems);
    }

    private boolean isUploadingActive;

    public void handleUploadFiles() throws IOException, InterruptedException, GeneralSecurityException {
        var selectedEmail = uploadView.getAccountChoiceBoxValue();
        if (!isEmail(selectedEmail)) {
            fxAlert.informationAlert(
                    "No email selected",
                    "Select an email to upload files"
            );
            return;
        }

        var requiredFileNames = uploadView.getFileListViewItems();
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
        CompletableFuture<DriveUploadResponse> uploadTask =
                getUploadTask(selectedEmail, requiredFiles, zipName);

        uploadTask.thenAcceptAsync(driveUploadResponse -> {
            executePostUploadTasks();
            if (driveUploadResponse.isUploadSuccess())
                executeUploadFinishedTasks(driveUploadResponse);
            else Platform.runLater(this::showUploadCancelledAlert);
        }).exceptionallyAsync(this::executeUploadTaskException);
    }

    private CompletableFuture<DriveUploadResponse> getUploadTask(String selectedEmail, List<File> requiredFiles, String zipName) throws IOException {
        if (requiredFiles.size() > 1)
            return linkShareModel.uploadFilesToGoogleDrive(selectedEmail, requiredFiles,
                    zipName);
        else
            return linkShareModel.uploadFileToGoogleDrive(selectedEmail,
                    requiredFiles.get(0));
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
        return uploadView.showTextInputDialog(
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

    @Autowired
    private ApplicationEventPublisher publisher;

    private void executeUploadFinishedTasks(DriveUploadResponse driveUploadResponse) {
        System.out.println("Upload finished...");
        publisher.publishEvent(new FileUploadSuccessEvent(this, driveUploadResponse));
        totalAddedFiles = new ArrayList<>();
        Platform.runLater(() -> {
            uploadView.clearFileListViewItems();
            showUploadFinishedAlert(driveUploadResponse.getSharableLink());
        });
    }

    private void executePostUploadTasks() {
        isUploadingActive = false;
        Platform.runLater(() -> {
            uploadView.setFileUploadProgressBarVisible(false);
            uploadView.updateFileUploadProgressBar(0.0);
            uploadView.setUploadBTNText("Upload files");
            enableRequiredUIElements();
        });
    }

    private void executePreUploadTasks() {
        disableRequiredUIElements();
        uploadView.setFileUploadProgressBarVisible(true);
        isUploadingActive = true;
        uploadView.setUploadBTNText("Cancel");
        System.out.println("Uploading.........");
    }

    private void cancelUpload() {
        uploadView.setUploadBTNText("Upload files");
        linkShareModel.cancelUploadFiles();
    }

    private void showUploadCancelledAlert() {
        fxAlert.informationAlert(
                "Upload cancelled",
                "Upload has been cancelled!"
        );
    }

    private void showUploadFinishedAlert(String link) {
        var copyButton = new ButtonType("Copy");
        fxAlert.confirmationAlert(
                "Successfully files are uploaded!",
                "By default files are uploaded to a default folder \n" +
                        " and sharable to anyone with viewer access via link.",
                "Copy the below link: " + link,
                () -> {copyToClipBoard(link);},
                copyButton, copyButton, ButtonType.OK
        );
    }

    private void copyToClipBoard(String link) {
        var clipBoardContent = new ClipboardContent();
        clipBoardContent.putString(link);
        Clipboard.getSystemClipboard().setContent(clipBoardContent);
    }

    private void disableRequiredUIElements() {
        uploadView.disableAccountChoiceBox(true);
        uploadView.disableAccountDisconnectBTN(true);
        uploadView.disableAddFilesBTN(true);
        uploadView.disableRemoveFilesBTN(true);
        uploadView.disableClearFilesBTN(true);
        uploadView.disableFileListView(true);
    }

    private void enableRequiredUIElements() {
        uploadView.disableAccountChoiceBox(false);
        uploadView.disableAccountDisconnectBTN(false);
        uploadView.disableAddFilesBTN(false);
        uploadView.disableRemoveFilesBTN(false);
        uploadView.disableClearFilesBTN(false);
        uploadView.disableFileListView(false);
    }

    public void handleClearListView() {
        if (uploadView.isFileListViewEmpty()) {
            fxAlert.informationAlert(
                    "No files to clear",
                    "Add some files to clear"
            );
            return;
        }

        uploadView.setFileListViewItems(FXCollections.observableArrayList());
        totalAddedFiles = new ArrayList<>();
    }

    public void updateProgressBar(double value) {
        if (isUploadingActive)
            uploadView.updateFileUploadProgressBar(value);
    }

    public void handleChangeToHistoryScene() throws IOException {
        TWFSFxApplication.loadScene("/templates/fx/Manage.fxml");
    }

    @Value("${twfs.feedback-url}")
    private String feedbackUrl;

    public void handleOpenFeedback() {
        var hostServices = tWinFileShareApplication.getHostServices();
        hostServices.showDocument(feedbackUrl);
    }
}
