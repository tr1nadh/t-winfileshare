package com.example.twinfileshare.fx.presenter;

import com.example.twinfileshare.entity.GoogleUserCRED;
import com.example.twinfileshare.entity.SharedFile;
import com.example.twinfileshare.fx.alert.FxAlert;
import com.example.twinfileshare.fx.view.ManageView;
import com.example.twinfileshare.repository.GoogleUserCREDRepository;
import com.example.twinfileshare.repository.ManageRepository;
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
import java.util.Collections;

@Component
public class ManagePresenter {

    @Autowired
    private ManageView view;

    @PostConstruct
    public void setManageView() {
        view.setManagePresenter(this);
    }

    @Autowired
    private ManageRepository repository;

    public void init() {
        var accounts = userCREDRepository.getAllEmails();
        view.addItemsToAccountChoiceBox(accounts);
    }

    public void addFile(SharedFile sharedFile) {
        view.addFileToListView(sharedFile);
    }

    public void handleCopyLinkToClipboard(ActionEvent event) {
        var selectedSharedFile = view.getSelectedSharedFile();
        if (selectedSharedFile == null) {
            Notifications.create()
                    .text("Select a file to copy sharable link")
                    .owner(event.getSource())
                    .showInformation();
            return;
        }

        if (Strings.isNullOrEmpty(selectedSharedFile.getSharableLink())) {
            Notifications.create()
                    .text("Sharing not enabled in selected file")
                    .owner(event.getSource())
                    .showInformation();
            return;
        }

        var clipBoardContent = new ClipboardContent();
        clipBoardContent.putString(selectedSharedFile.getSharableLink());
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
        var selectedSharedFile = view.getSelectedSharedFile();
        if (selectedSharedFile == null) {
            Notifications.create()
                    .text("Select a file to stop file sharing")
                    .owner(event.getSource())
                    .showInformation();
            return;
        }

        var email = selectedSharedFile.getEmail();
        if (getGoogleUserCRED(email) == null) {
            Notifications.create()
                    .text("Account '" + email + "' is disconnected, cannot stop file sharing.")
                    .owner(event.getSource())
                    .showInformation();
            return;
        }

        if (Strings.isNullOrEmpty(selectedSharedFile.getSharableLink())) {
            Notifications.create()
                    .text("File sharing has been stopped")
                    .owner(event.getSource())
                    .showInformation();
            return;
        }

        driveService.deleteFilePermissions(selectedSharedFile.getEmail(), selectedSharedFile.getId());
        selectedSharedFile.setSharableLink(null);
        repository.save(selectedSharedFile);

        Notifications.create()
                .text("File sharing has been stopped")
                .owner(event.getSource())
                .showInformation();
    }

    private GoogleUserCRED getGoogleUserCRED(String email) {
        return userCREDRepository.findByEmail(email);
    }

    public void handleStartFileSharing(ActionEvent event) throws IOException {
        var selectedSharedFile = view.getSelectedSharedFile();
        if (selectedSharedFile == null) {
            Notifications.create()
                    .text("Select a file to start file sharing with anyone")
                    .owner(event.getSource())
                    .showInformation();
            return;
        }

        var email = selectedSharedFile.getEmail();
        if (getGoogleUserCRED(email) == null) {
            Notifications.create()
                    .text("Account '" + email + "' is disconnected, cannot start file sharing.")
                    .owner(event.getSource())
                    .showInformation();
            return;
        }

        if (!Strings.isNullOrEmpty(selectedSharedFile.getSharableLink())) {
            Notifications.create()
                    .text("Selected file already is in sharing")
                    .owner(event.getSource())
                    .showInformation();
            return;
        }

        driveService.enableFileSharingWithAnyone(
                selectedSharedFile.getEmail(),
                selectedSharedFile.getId());

        Notifications.create()
                .text("File sharing has been started")
                .owner(event.getSource())
                .showInformation();
    }

    @Autowired
    private FxAlert fxAlert;

    public void handleDeleteFile(ActionEvent event) throws IOException {
        var selectedSharedFile = view.getSelectedSharedFile();
        if (selectedSharedFile == null) {
            Notifications.create()
                    .text("Select a file to delete")
                    .owner(event.getSource())
                    .showInformation();
            return;
        }

        var email = selectedSharedFile.getEmail();
        if (getGoogleUserCRED(email) == null) {
            Notifications.create()
                    .text("Account '" + email + "' is disconnected, cannot delete file.")
                    .owner(event.getSource())
                    .showInformation();
            return;
        }

        fxAlert.confirmationAlert(
                "Delete history file",
                "Are you sure, you want to delete '" + selectedSharedFile.getFilename() + "' ?",
                "Deleting file here, will also delete in the cloud",
                () -> {
                    try {
                        deleteHistoryFile(selectedSharedFile, event);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    private void deleteHistoryFile(SharedFile selectedSharedFile, ActionEvent event) throws IOException {
        driveService.deleteFile(selectedSharedFile.getEmail(), selectedSharedFile.getId());
        System.out.println(selectedSharedFile);
        view.deleteFileFromListView(selectedSharedFile);
        repository.delete(selectedSharedFile);

        Notifications.create()
                .text("File has been deleted")
                .owner(event.getSource())
                .showInformation();
    }

    public void refresh(String email) throws IOException {
        var dbSharedFilesIds = repository.findAllIds();
        var cloudSharedFiles = driveService.fetchFilesFromCloud(email);
        for (var file : cloudSharedFiles) {
            if (dbSharedFilesIds.contains(file.getId()))
                continue;

            var filename = file.getName();
            var id = file.getId();
            var link = file.getWebViewLink();
            var sharedFile = new SharedFile(id, filename, link, email);
            repository.save(sharedFile);
        }
    }

    public void handleLoadingSharedFiles(String currentValue) {
        var items = repository.getByEmail(currentValue);
        Collections.reverse(items);
        view.setItemsToListView(items);
    }

    public void loadAccountSharedFiles(String selectedValue) {
        var indexOfSelectedValue = view.getAccountChoiceBoxIndexOf(selectedValue);
        view.setAccountByIndex(indexOfSelectedValue);
    }

    public void addAccount(String email) {
        view.addItemToAccountChoiceBox(email);
    }

    public String getCurrentSelectedAccount() {
        return view.getCurrentSelectedAccount();
    }

    public void removeAccountFromChoiceBox(String email) {
        view.removeItemFromChoiceBox(email);
        view.setAccountChoiceBoxValue("Select an email");
    }
}
