package com.example.twinfileshare.fx;

import com.example.twinfileshare.fx.view.HistoryView;
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

    public void HandleChangeToMainScene() throws IOException {
        TWFSFxApplication.loadScene("/templates/fx/Main.fxml");
    }
}
