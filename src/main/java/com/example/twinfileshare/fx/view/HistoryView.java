package com.example.twinfileshare.fx.view;

import com.example.twinfileshare.entity.HistoryFile;
import com.example.twinfileshare.fx.HistoryPresenter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Controller
public class HistoryView implements Initializable {

    private HistoryPresenter presenter;

    public void changeToMainScene(ActionEvent event) throws IOException {
        presenter.HandleChangeToMainScene();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        presenter.init();
    }

    public void copyLinkToClipboard(ActionEvent event) throws InterruptedException {
        presenter.handleCopyLinkToClipboard(event);
    }

    public void stopFileSharing(ActionEvent event) throws IOException {
        presenter.handleStopFileSharing(event);
    }

    public void startFileSharing(ActionEvent event) throws IOException {
        presenter.handleStartFileSharing(event);
    }

    public void deleteFile(ActionEvent event) throws IOException {
        presenter.handleDeleteFile(event);
    }

    @FXML
    private ListView<HistoryFile> historyListView = new ListView<>();

    public HistoryFile getSelectedHistoryFile() {
        return historyListView.getSelectionModel().getSelectedItem();
    }

    public void addFileToListView(HistoryFile historyFile) {
        historyListView.getItems().add(historyFile);
    }

    public void addFilesToListView(List<HistoryFile> historyFiles) {
        historyListView.getItems().addAll(historyFiles);
    }

    public void setHistoryPresenter(HistoryPresenter presenter) {
        this.presenter = presenter;
    }
}
