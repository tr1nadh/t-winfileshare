package com.example.twinfileshare.fx;

public interface FxAlert {
    void confirmationAlert(
            String title,
            String header,
            String content,
            Runnable run
    );
}
