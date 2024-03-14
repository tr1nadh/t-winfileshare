package com.example.twinfileshare.fx.presenter;

import com.example.twinfileshare.fx.TWFSFxApplication;
import com.example.twinfileshare.fx.view.UploadProgressView;
import jakarta.annotation.PostConstruct;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
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
        uploadStage.setOnCloseRequest(this::handleOnCloseRequest);
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

    private void handleOnCloseRequest(WindowEvent windowEvent) {
        uploadStage.hide();
        var alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setResizable(false);
        alert.setTitle("Cancellation alert");
        alert.setHeaderText("Are you sure you want to cancel?");
        alert.showAndWait()
                .filter(res -> res == ButtonType.OK)
                .ifPresentOrElse(this::onCloseRequest,
                        () -> uploadStage.showAndWait());
    }

    private void onCloseRequest(ButtonType buttonType) {
        handleCancelUpload();
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
