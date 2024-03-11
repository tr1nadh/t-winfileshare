package com.example.twinfileshare.fx.presenter;

import com.example.twinfileshare.event.payload.UploadCancelledEvent;
import com.example.twinfileshare.fx.TWFSFxApplication;
import com.example.twinfileshare.fx.view.UploadView;
import jakarta.annotation.PostConstruct;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import java.io.IOException;

public class UploadPresenter {

    @Autowired
    private UploadView uploadView;

    private final Stage uploadStage = new Stage();

    private boolean isUploadActive;

    public void start(Stage stage) {
        isUploadActive = true;
        try {
            Scene scene = TWFSFxApplication.generateScene("/templates/fx/UploadFiles.fxml");
            uploadStage.setScene(scene);
            uploadStage.initModality(Modality.WINDOW_MODAL);
            uploadStage.initOwner(stage);
            uploadStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateProgress(double progress) {
        uploadView.updateProgressBar(progress);
    }

    public void updateLog(String value) {
        uploadView.setUploadLogText(value);
    }

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void handleCancelUpload(ActionEvent event) {
        eventPublisher.publishEvent(new UploadCancelledEvent(this));

        close();
    }

    public boolean isUploadActive() {
        return isUploadActive;
    }

    @PostConstruct
    private void setUploadView() {
        uploadView.setUploadPresenter(this);
    }

    public void close() {
        isUploadActive = false;

        uploadStage.close();
    }
}
