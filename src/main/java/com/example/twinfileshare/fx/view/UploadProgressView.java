package com.example.twinfileshare.fx.view;

import com.example.twinfileshare.fx.presenter.UploadProgressPresenter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.springframework.stereotype.Controller;

@Controller
public class UploadProgressView {

    @FXML
    private ProgressBar uploadProgressBar;

    @FXML
    private Label uploadLogText;

    private UploadProgressPresenter uploadProgressPresenter;

    public void updateProgressBar(double progress) {
        uploadProgressBar.setProgress(progress);
    }

    public void setUploadLogText(String value) {
        uploadLogText.setText(value);
    }

    public void cancelUpload(ActionEvent event) {
        uploadProgressPresenter.handleCancelUpload();
    }

    public void setUploadPresenter(UploadProgressPresenter uploadProgressPresenter) {
        this.uploadProgressPresenter = uploadProgressPresenter;
    }
}
