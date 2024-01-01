package com.example.twinfileshare.fx;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.springframework.stereotype.Component;

@Component
public class FxAlertService implements FxAlert {

    @Override
    public void confirmationAlert(String title, String header, String content, Runnable okRun) {
        var alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setResizable(false);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait()
                .filter(res -> res == ButtonType.OK)
                .ifPresent(res -> okRun.run());
    }
}
