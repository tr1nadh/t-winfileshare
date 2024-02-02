package com.example.twinfileshare.fx.presenter;

import com.example.twinfileshare.entity.GoogleUserCRED;
import com.example.twinfileshare.entity.HistoryFile;
import com.example.twinfileshare.fx.TWFSFxApplication;
import com.example.twinfileshare.fx.alert.FxAlert;
import com.example.twinfileshare.fx.view.HistoryView;
import com.example.twinfileshare.repository.GoogleUserCREDRepository;
import com.example.twinfileshare.repository.HistoryRepository;
import com.example.twinfileshare.service.GoogleDriveService;
import com.google.api.client.util.Strings;
import jakarta.annotation.PostConstruct;
import javafx.event.ActionEvent;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.controlsfx.control.Notifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HistoryPresenter {

    @Autowired
    private HistoryView view;

    @PostConstruct
    public void setHistoryView() {
        view.setHistoryPresenter(this);
    }

    @Autowired
    private HistoryRepository repository;

    public void init() {
        view.addFilesToListView(repository.findAll().reversed());
    }

    public void HandleChangeToMainScene() throws IOException {
        TWFSFxApplication.loadScene("/templates/fx/Main.fxml");
    }

    public void addFile(HistoryFile historyFile) {
        view.addFileToListView(historyFile);
    }

    public void handleCopyLinkToClipboard(ActionEvent event) {
        var selectedHistoryFile = view.getSelectedHistoryFile();
        if (selectedHistoryFile == null) {
            Notifications.create()
                    .text("Select a file to copy sharable link")
                    .owner(event.getSource())
                    .showInformation();
            return;
        }

        if (Strings.isNullOrEmpty(selectedHistoryFile.getSharableLink())) {
            Notifications.create()
                    .text("Sharing not enabled in selected file")
                    .owner(event.getSource())
                    .showInformation();
            return;
        }

        var clipBoardContent = new ClipboardContent();
        clipBoardContent.putString(selectedHistoryFile.getSharableLink());
        Clipboard.getSystemClipboard().setContent(clipBoardContent);
        Notifications.create()
                .text("Sharable link copied to clipboard!")
                .owner(event.getSource())
                .showInformation();
    }

    @Autowired
    private GoogleDriveService driveService;

    @Autowired
    private GoogleUserCREDRepository userCREDRepository;

    public void handleStopFileSharing(ActionEvent event) throws IOException {
        var selectedHistoryFile = view.getSelectedHistoryFile();
        if (selectedHistoryFile == null) {
            Notifications.create()
                    .text("Select a file to stop file sharing")
                    .owner(event.getSource())
                    .showInformation();
            return;
        }

        var email = selectedHistoryFile.getEmail();
        if (getGoogleUserCRED(email) == null) {
            Notifications.create()
                    .text("Account '" + email + "' is disconnected, cannot stop file sharing.")
                    .owner(event.getSource())
                    .showInformation();
            return;
        }

        if (Strings.isNullOrEmpty(selectedHistoryFile.getSharableLink())) {
            Notifications.create()
                    .text("File sharing has been stopped")
                    .owner(event.getSource())
                    .showInformation();
            return;
        }

        driveService.deleteFilePermissions(selectedHistoryFile.getEmail(), selectedHistoryFile.getId());
        selectedHistoryFile.setSharableLink(null);
        repository.save(selectedHistoryFile);

        Notifications.create()
                .text("File sharing has been stopped")
                .owner(event.getSource())
                .showInformation();
    }

    private GoogleUserCRED getGoogleUserCRED(String email) {
        return userCREDRepository.findByEmail(email);
    }

    public void handleStartFileSharing(ActionEvent event) throws IOException {
        var selectedHistoryFile = view.getSelectedHistoryFile();
        if (selectedHistoryFile == null) {
            Notifications.create()
                    .text("Select a file to start file sharing with anyone")
                    .owner(event.getSource())
                    .showInformation();
            return;
        }

        var email = selectedHistoryFile.getEmail();
        if (getGoogleUserCRED(email) == null) {
            Notifications.create()
                    .text("Account '" + email + "' is disconnected, cannot start file sharing.")
                    .owner(event.getSource())
                    .showInformation();
            return;
        }

        if (!Strings.isNullOrEmpty(selectedHistoryFile.getSharableLink())) {
            Notifications.create()
                    .text("Selected file already is in sharing")
                    .owner(event.getSource())
                    .showInformation();
            return;
        }

        driveService.fileShareWithAnyone(
                selectedHistoryFile.getId(),
                selectedHistoryFile.getEmail(),
                selectedHistoryFile.getFilename());

        Notifications.create()
                .text("File sharing has been started")
                .owner(event.getSource())
                .showInformation();
    }

    @Autowired
    private FxAlert fxAlert;

    public void handleDeleteFile(ActionEvent event) throws IOException {
        var selectedHistoryFile = view.getSelectedHistoryFile();
        if (selectedHistoryFile == null) {
            Notifications.create()
                    .text("Select a file to delete")
                    .owner(event.getSource())
                    .showInformation();
            return;
        }

        var email = selectedHistoryFile.getEmail();
        if (getGoogleUserCRED(email) == null) {
            Notifications.create()
                    .text("Account '" + email + "' is disconnected, cannot delete file.")
                    .owner(event.getSource())
                    .showInformation();
            return;
        }

        fxAlert.confirmationAlert(
                "Delete history file",
                "Are you sure, you want to delete '" + selectedHistoryFile.getFilename() + "' ?",
                "Deleting file here, will also delete in the cloud",
                () -> {
                    try {
                        deleteHistoryFile(selectedHistoryFile, event);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    private void deleteHistoryFile(HistoryFile selectedHistoryFile, ActionEvent event) throws IOException {
        driveService.deleteFile(selectedHistoryFile.getEmail(), selectedHistoryFile.getId());
        view.deleteFileFromListView(selectedHistoryFile);
        repository.delete(selectedHistoryFile);

        Notifications.create()
                .text("File has been deleted")
                .owner(event.getSource())
                .showInformation();
    }

}
