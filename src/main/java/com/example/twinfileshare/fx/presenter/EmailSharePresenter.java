package com.example.twinfileshare.fx.presenter;

import com.example.twinfileshare.SendMessage;
import com.example.twinfileshare.event.payload.FileUploadSuccessEvent;
import com.example.twinfileshare.fx.TWFSFxApplication;
import com.example.twinfileshare.fx.alert.FxAlert;
import com.example.twinfileshare.fx.model.EmailShareModal;
import com.example.twinfileshare.fx.model.LinkShareModel;
import com.example.twinfileshare.fx.view.EmailShareView;
import com.example.twinfileshare.service.DriveUploadResponse;
import com.example.twinfileshare.utility.Strings;
import jakarta.annotation.PostConstruct;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailSharePresenter {

    @Autowired
    private EmailShareView emailShareView;

    @Autowired
    private EmailShareModal emailShareModal;

    @PostConstruct
    public void setView() {
        emailShareView.setEmailSharePresenter(this);
    }

    public void init() {
        emailShareView.setFromAccountChoiceBoxItems(emailShareModal.getAllEmails());
    }

    @Autowired
    private TWFSFxApplication twfsFxApplication;


    public void handleManageAccounts(ActionEvent event) {
        var node = (Node) event.getSource();
        var stage = (Stage) node.getScene().getWindow();
        try {
            Stage dialog = new Stage();
            Scene scene = TWFSFxApplication.generateScene("/templates/fx/AccountManage.fxml");
            dialog.setScene(scene);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(stage);
            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Autowired
    private SendMessage sendMessage;

    @Autowired
    private LinkSharePresenter presenter;

    public void removeAccountFromChoiceBox(String email) {
        emailShareView.removeItemFromChoiceBox(email);
    }

    public void handleAddFilesFilesFromFileManager(ActionEvent event) {
        var selectedFiles = emailShareView.openMultipleFileChooserWindow(
                "Select files to upload", event);

        if (selectedFiles != null) emailShareView.addItemsToFileListView(selectedFiles);
    }

    @Autowired
    private FxAlert fxAlert;

    public void handleRemoveFilesFromListView() {
        var listViewItems = emailShareView.getFileListViewItems();
        if (listViewItems.isEmpty()) {
            fxAlert.informationAlert(
                    "Cannot remove",
                    "Add some files to remove"
            );
            return;
        }

        var selectedListViewItems = emailShareView.getSelectedFileListViewItems();
        listViewItems.removeAll(selectedListViewItems);
    }

    public void handleClearListView() {
        if (emailShareView.isFileListViewEmpty()) {
            fxAlert.informationAlert(
                    "No files to clear",
                    "Add some files to clear"
            );
            return;
        }

        emailShareView.removeAllFileListViewItems();
    }

    private UploadProgressPresenter uploadProgressPresenter;

    @Autowired
    private LinkShareModel linkShareModel;

    public void handleSendAndShare(ActionEvent event) throws IOException {
        var isRequiredFieldsThere = checkRequiredFields();
        if (!isRequiredFieldsThere) return;

        var selectedEmail = emailShareView.getAccountChoiceBoxValue();
        if (!isEmail(selectedEmail)) {
            fxAlert.informationAlert(
                    "No email selected",
                    "Select an email to upload files"
            );
            return;
        }

        var requiredFiles = emailShareView.getFileListViewItems();
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

        Platform.runLater(() -> uploadProgressPresenter.updateLog("uploading files..."));

        executeUpload(requiredFiles, selectedEmail, zipName);
    }

    private boolean checkRequiredFields() {
        var title = emailShareView.getTitleTextValue();
        var bodyText = emailShareView.getMessageBodyTextValue();
        var toText = emailShareView.getToTextValue();
        if (Strings.isEmptyOrWhitespace(title)) {
            fxAlert.errorAlert("Field error", "Title cannot be empty", "");
            return false;
        } else if (Strings.isEmptyOrWhitespace(bodyText)) {
            fxAlert.errorAlert("Field error", "Message cannot be empty", "");
            return false;
        } else if (Strings.isEmptyOrWhitespace(toText) || !toText.contains("@")) {
            fxAlert.errorAlert("Field error", "To email cannot be empty", "");
            return false;
        } else if (toText.split("@").length > 2) {
            fxAlert.errorAlert("Field error", "To email doesn't support multiple emails, right now",
                    "");
            return false;
        }

        return true;
    }

    private boolean isEmail(String currentSelectedEmail) {
        return currentSelectedEmail.contains("@");
    }

    private void executeUpload(List<File> requiredFiles, String selectedEmail, String zipName) throws IOException {
        CompletableFuture<DriveUploadResponse> uploadTask =
                getUploadTask(selectedEmail, requiredFiles, zipName);

        uploadTask.thenAcceptAsync(driveUploadResponse -> {
            if (driveUploadResponse.isUploadSuccess()) {
                Platform.runLater(() -> uploadProgressPresenter.updateLog("uploading finished..."));
                try {
                    executeUploadFinishedTasks(driveUploadResponse);
                } catch (MessagingException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
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

    private void executeUploadFinishedTasks(DriveUploadResponse driveUploadResponse) throws MessagingException, IOException {
        System.out.println("Upload finished...");
        publisher.publishEvent(new FileUploadSuccessEvent(this, driveUploadResponse));
        sendEmail(driveUploadResponse);
        var toEmail = emailShareView.getToTextValue();
        Platform.runLater(() -> {
            emailShareView.removeAllFileListViewItems();
            showEmailSentAlert(toEmail);
            uploadProgressPresenter.updateLog("email sent!");
            closeUpload();
            clearTheFields();
        });
    }

    private void clearTheFields() {
        emailShareView.setFromAccountChoiceBoxValue("Select an email");
        emailShareView.setToTextValue("");
        emailShareView.setTitleTextValue("");
        emailShareView.setBodyTextValue("");
    }

    public void updateProgress(double progress) {
        if (!uploadProgressPresenter.isUploadActive()) {
            System.out.println("Uploading is not active");
            return;
        }

        uploadProgressPresenter.updateProgress(progress);
    }

    private void sendEmail(DriveUploadResponse driveUploadResponse) throws MessagingException, IOException {
        var fromEmail = emailShareView.getSelectedFromAccountChoiceBoxItem();
        var toEmail = emailShareView.getToTextValue();
        var subject = emailShareView.getTitleTextValue();
        var body = emailShareView.getMessageBodyTextValue();
        var message = sendMessage.sendEmail(fromEmail,
                toEmail, subject, body, driveUploadResponse.getSharableLink());
        System.out.println(message);
    }

    private CompletableFuture<DriveUploadResponse> getUploadTask(String selectedEmail, List<File> requiredFiles, String zipName) throws IOException {
        if (requiredFiles.size() > 1)
            return linkShareModel.uploadFilesToGoogleDrive(selectedEmail, requiredFiles,
                    zipName);
        else
            return linkShareModel.uploadFileToGoogleDrive(selectedEmail,
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
        return emailShareView.showTextInputDialog(
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

    private void showEmailSentAlert(String email) {
        fxAlert.informationAlert(
                "Email sent!",
                "Successfully sent the email to: " + email
        );
    }

    @Autowired
    public void setUploadPresenter(UploadProgressPresenter uploadProgressPresenter) {
        this.uploadProgressPresenter = uploadProgressPresenter;
        uploadProgressPresenter.setCancellable(this::cancelUpload);
    }

    public void addAccount(String email) {
        emailShareView.addItemToFromAccountChoiceBox(email);
    }
}
