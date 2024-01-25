package com.example.twinfileshare.fx;

import com.example.twinfileshare.fx.view.HistoryView;
import com.example.twinfileshare.entity.HistoryFile;
import com.example.twinfileshare.repository.HistoryRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HistoryPresenter {

    @Autowired
    private HistoryView view;

    @PostConstruct
    public void setHistoryView() {
        view.setPresenter(this);
    }

    @Autowired
    private HistoryRepository repository;

    public void init() {
        view.addFilesToListView(repository.findAll());
    }

    public void HandleChangeToMainScene() throws IOException {
        TWFSFxApplication.loadScene("/templates/fx/Main.fxml");
    }

    public void addFile(HistoryFile historyFile) {
        view.addFileToListView(historyFile);
    }
}
