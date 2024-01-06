package com.example.twinfileshare.fx;

import javafx.scene.control.ButtonType;

public interface FxAlert {
    void confirmationAlert(
            String title,
            String header,
            String content,
            Runnable okRun
    );

    void confirmationAlert(
            String title,
            String header,
            String content,
            Runnable okRun,
            ButtonType okButton, ButtonType... buttonTypes
    );

    void informationAlert(
            String title,
            String header
    );

    void errorAlert(
            String title,
            String header,
            String content
    );
}
