package com.example.twinfileshare.fx.presenter;

import com.example.twinfileshare.event.payload.UploadCancelledEvent;
import com.example.twinfileshare.fx.view.UploadView;
import jakarta.annotation.PostConstruct;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class UploadPresenter {

    @Autowired
    private UploadView uploadView;

    public void updateProgressBar(double progress) {
        uploadView.updateProgressBar(progress);
    }

    public void updateLog(String value) {
        uploadView.setUploadLogText(value);
    }

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void handleCancelUpload(ActionEvent event) {
        eventPublisher.publishEvent(new UploadCancelledEvent(this));

        var node = (Node) event.getSource();
        var stage = (Stage) node.getScene().getWindow();
        stage.close();
    }

    @PostConstruct
    private void setUploadView() {
        uploadView.setUploadPresenter(this);
    }
}
