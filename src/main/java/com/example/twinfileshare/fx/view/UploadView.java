package com.example.twinfileshare.fx.view;

import com.example.twinfileshare.fx.presenter.UploadPresenter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.springframework.stereotype.Controller;

@Controller
public class UploadView {

    @FXML
    private ProgressBar uploadProgressBar;

    @FXML
    private Label uploadLogText;

    private UploadPresenter uploadPresenter;

    public void updateProgressBar(double progress) {
        uploadProgressBar.setProgress(progress);
    }

    public void setUploadLogText(String value) {
        uploadLogText.setText(value);
    }

    public void cancelUpload(ActionEvent event) {
        uploadPresenter.handleCancelUpload();
    }

    public void setUploadPresenter(UploadPresenter uploadPresenter) {
        this.uploadPresenter = uploadPresenter;
    }
}
