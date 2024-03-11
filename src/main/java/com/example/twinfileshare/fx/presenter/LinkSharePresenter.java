package com.example.twinfileshare.fx.presenter;

import com.example.twinfileshare.TWinFileShareApplication;
import com.example.twinfileshare.event.payload.FileUploadSuccessEvent;
import com.example.twinfileshare.event.payload.SelectedAccountChanged;
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
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
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

    private boolean isEmail(String currentSelectedEmail) {
        return currentSelectedEmail.contains("@");
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

    private UploadPresenter uploadPresenter;

    public void handleUploadFiles(ActionEvent event) throws IOException {
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

        startUpload(event);

        executeUpload(requiredFileNames, selectedEmail, zipName);
    }

    private void executeUpload(ObservableList<String> requiredFileNames, String selectedEmail, String zipName) throws IOException {
        var requiredFiles = getRequiredFiles(requiredFileNames);
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
        totalAddedFiles = new ArrayList<>();
        Platform.runLater(() -> {
            uploadView.clearFileListViewItems();
            closeUpload();
            showUploadFinishedAlert(driveUploadResponse.getSharableLink());
        });
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

    private void closeUpload() {
        uploadPresenter.closeWindow();
    }

    private void startUpload(ActionEvent event) {
        var node = (Node) event.getSource();
        var stage = (Stage) node.getScene().getWindow();
        uploadPresenter.start(stage);
    }

    private void openDialog(ActionEvent event, String resource) {
        var node = (Node) event.getSource();
        var stage = (Stage) node.getScene().getWindow();
        try {
            Stage dialog = new Stage();
            Scene scene = TWFSFxApplication.generateScene(resource);
            dialog.setScene(scene);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.initOwner(stage);
            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        linkShareModel.cancelUploadFiles();
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

    public void handleAccountChoiceBoxValueChanged(String selectedValue) {
        publisher.publishEvent(new SelectedAccountChanged(this, selectedValue));
    }

    public void  handleOpenManageAccountsDialog(ActionEvent event) {
        openDialog(event, "/templates/fx/AccountManage.fxml");
    }

    public void removeAccountFromChoiceBox(String email) {
        uploadView.removeItemFromAccountChoiceBox(email);
    }

    public void updateProgress(double progress) {
        if (!uploadPresenter.isUploadActive()) {
            System.out.println("Uploading is not active");
            return;
        }

        uploadPresenter.updateProgress(progress);
    }

    @Autowired
    public void setUploadPresenter(UploadPresenter uploadPresenter) {
        this.uploadPresenter = uploadPresenter;
        uploadPresenter.setCancellable(this::cancelUpload);
    }
}
