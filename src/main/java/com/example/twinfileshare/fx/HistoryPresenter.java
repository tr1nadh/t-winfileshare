package com.example.twinfileshare.fx;

import com.example.twinfileshare.entity.HistoryFile;
import com.example.twinfileshare.fx.view.HistoryView;
import com.example.twinfileshare.repository.HistoryRepository;
import com.example.twinfileshare.service.GoogleDriveService;
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

    public void handleStopFileSharing(ActionEvent event) throws IOException {
        var selectedHistoryFile = view.getSelectedHistoryFile();
        if (selectedHistoryFile == null) {
            Notifications.create()
                    .text("Select a file to stop file sharing")
                    .owner(event.getSource())
                    .showInformation();
            return;
        }

        driveService.deleteFilePermissions(selectedHistoryFile.getEmail(), selectedHistoryFile.getId());

        Notifications.create()
                .text("File sharing has been stopped")
                .owner(event.getSource())
                .showInformation();
    }
}
