package com.example.twinfileshare.fx.presenter;

import com.example.twinfileshare.fx.TWFSFxApplication;
import com.example.twinfileshare.fx.view.UploadProgressView;
import jakarta.annotation.PostConstruct;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component()
public class UploadProgressPresenter {

    @Autowired
    private UploadProgressView uploadProgressView;

    private Stage uploadStage;

    private boolean isUploadActive;

    @Setter
    private Runnable cancellable;

    public void start(Stage owner) {
        this.uploadStage = new Stage();
        isUploadActive = true;

        try {
            Scene scene = TWFSFxApplication.generateScene("/templates/fx/UploadProgressView.fxml");
            uploadStage.setScene(scene);
            uploadStage.initModality(Modality.APPLICATION_MODAL);
            uploadStage.setMaximized(false);
            uploadStage.initOwner(owner);
            uploadStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateProgress(double progress) {
        uploadProgressView.updateProgressBar(progress);
    }

    public void updateLog(String value) {
        uploadProgressView.setUploadLogText(value);
    }

    public void handleCancelUpload() {
        if (cancellable != null) cancellable.run();
        closeWindow();
    }

    public boolean isUploadActive() {
        return isUploadActive;
    }

    @PostConstruct
    private void setUploadProgressView() {
        uploadProgressView.setUploadPresenter(this);
    }

    public void closeWindow() {
        isUploadActive = false;

        uploadStage.close();
    }
}
