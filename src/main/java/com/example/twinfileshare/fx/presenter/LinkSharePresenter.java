package com.example.twinfileshare.fx.presenter;

import com.example.twinfileshare.event.payload.FileUploadSuccessEvent;
import com.example.twinfileshare.event.payload.SelectedAccountChanged;
import com.example.twinfileshare.fx.alert.FxAlert;
import com.example.twinfileshare.service.LinkShareService;
import com.example.twinfileshare.fx.view.ILinkShareView;
import com.example.twinfileshare.modal.GoogleUserCredModal;
import com.example.twinfileshare.google.DriveUploadResponse;
import com.example.twinfileshare.utility.Strings;
import jakarta.annotation.PostConstruct;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class LinkSharePresenter {

    @Autowired
    private ILinkShareView uploadView;
    @Autowired
    private LinkShareService linkShareService;

    @Autowired
    private FxAlert fxAlert;

    @PostConstruct
    public void setView() {
        uploadView.setUploadPresenter(this);
    }

    @Autowired
    private GoogleUserCredModal googleUserCredModal;

    public void init() {
        uploadView.setAccountChoiceBoxItems(googleUserCredModal.getAllEmails());
        uploadView.setFileListViewSelectionMode(SelectionMode.MULTIPLE);
    }

    private boolean isEmail(String currentSelectedEmail) {
        return currentSelectedEmail.contains("@");
    }

    public void handleAddFilesFilesFromFileManager(ActionEvent event) {
        var selectedFiles = uploadView.openMultipleFileChooserWindow(
                "Select files to upload", event);

        if (selectedFiles != null) uploadView.addItemsToFileListView(selectedFiles);
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

    public void handleClearListView() {
        if (uploadView.isFileListViewEmpty()) {
            fxAlert.informationAlert(
                    "No files to clear",
                    "Add some files to clear"
            );
            return;
        }

        uploadView.removeAllFileListViewItems();
    }

    private UploadProgressPresenter uploadProgressPresenter;

    public void handleUploadFiles(ActionEvent event) throws IOException {
        var selectedEmail = uploadView.getAccountChoiceBoxValue();
        if (!isEmail(selectedEmail)) {
            fxAlert.informationAlert(
                    "No email selected",
                    "Select an email to upload files"
            );
            return;
        }

        var requiredFiles = uploadView.getFileListViewItems();
        if (requiredFiles.isEmpty()) {
            fxAlert.informationAlert(
                    "No files to upload",
                    "Add some files to upload"
            );
            return;
        }

        String zipName = null;
        if (requiredFiles.size() > 1) {
            zipName = getZipName();
            if (zipName == null) return;
            if (Strings.isEmptyOrWhitespace(zipName)) {
                fxAlert.errorAlert(
                        "Zip name error",
                        "Zip name cannot be empty or blank",
                        ""
                );

                return;
            }

        }

        startUpload(event);

        executeUpload(requiredFiles, selectedEmail, zipName);
    }

    private void executeUpload(List<File> requiredFiles, String selectedEmail, String zipName) throws IOException {
        CompletableFuture<DriveUploadResponse> uploadTask =
                getUploadTask(selectedEmail, requiredFiles, zipName);

        uploadTask.thenAcceptAsync(driveUploadResponse -> {
            if (driveUploadResponse.isUploadSuccess())
                executeUploadFinishedTasks(driveUploadResponse);
            else Platform.runLater(() -> {
                closeUpload();
                showUploadCancelledAlert();
            });
        }).exceptionallyAsync(ex -> {
                Platform.runLater(this::closeUpload);
                executeUploadTaskException(ex);
            return null;
        });
    }

    private void executeUploadTaskException(Throwable ex) {
        if (ex.getCause().getMessage().contains("Stream closed")) {
            Platform.runLater(this::showUploadCancelledAlert);
        } else {
            Platform.runLater(() -> {
                showUploadExceptionAlert("Unknown error. contact dev!!!");
            });
        }
    }

    private void showUploadCancelledAlert() {
        fxAlert.informationAlert(
                "Upload cancelled",
                "Upload has been cancelled!"
        );
    }

    private void executeUploadFinishedTasks(DriveUploadResponse driveUploadResponse) {
        System.out.println("Upload finished...");
        publisher.publishEvent(new FileUploadSuccessEvent(this, driveUploadResponse));
        Platform.runLater(() -> {
            uploadView.clearFileListViewItems();
            closeUpload();
            uploadView.setAccountChoiceBoxValue("Select an email");
            showUploadFinishedAlert(driveUploadResponse.getSharableLink());
        });
    }

    private CompletableFuture<DriveUploadResponse> getUploadTask(String selectedEmail, List<File> requiredFiles, String zipName) throws IOException {
        if (requiredFiles.size() > 1)
            return linkShareService.uploadFilesToGoogleDrive(selectedEmail, requiredFiles,
                    zipName);
        else
            return linkShareService.uploadFileToGoogleDrive(selectedEmail,
                    requiredFiles.get(0));
    }

    private void closeUpload() {
        uploadProgressPresenter.closeWindow();
    }

    private void startUpload(ActionEvent event) {
        var node = (Node) event.getSource();
        var stage = (Stage) node.getScene().getWindow();
        uploadProgressPresenter.start(stage);
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

    public void cancelUpload() {
        linkShareService.cancelUploadFiles();
    }

    private void showUploadFinishedAlert(String link) {
        var copyButton = new ButtonType("Copy sharable link");
        fxAlert.confirmationAlert(
                "Successfully files are uploaded!",
                "By default files are uploaded to a default folder \n" +
                        " and sharable to anyone with viewer access via link.",
                "",
                () -> {copyToClipBoard(link);},
                copyButton, copyButton, ButtonType.OK
        );
    }

    private void copyToClipBoard(String link) {
        var clipBoardContent = new ClipboardContent();
        clipBoardContent.putString(link);
        Clipboard.getSystemClipboard().setContent(clipBoardContent);
    }

    public void handleAccountChoiceBoxValueChanged(String selectedValue) {
        publisher.publishEvent(new SelectedAccountChanged(this, selectedValue));
    }

    public void removeAccountFromChoiceBox(String email) {
        uploadView.removeItemFromAccountChoiceBox(email);
        uploadView.setAccountChoiceBoxValue("Select an email");
    }

    @Autowired
    public void setUploadPresenter(UploadProgressPresenter uploadProgressPresenter) {
        this.uploadProgressPresenter = uploadProgressPresenter;
        uploadProgressPresenter.setCancellable(this::cancelUpload);
    }
}
