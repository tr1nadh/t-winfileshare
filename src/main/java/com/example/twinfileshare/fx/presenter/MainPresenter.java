package com.example.twinfileshare.fx.presenter;

import com.example.twinfileshare.TWinFileShareApplication;
import com.example.twinfileshare.fx.TWFSFxApplication;
import com.example.twinfileshare.fx.view.MainView;
import jakarta.annotation.PostConstruct;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class MainPresenter {

    @Autowired
    private MainView mainView;


    @PostConstruct
    public void setMainView() {
        mainView.setMainPresenter(this);
    }

    @Autowired
    private TWinFileShareApplication tWinFileShareApplication;

    @Value("${twfs.feedback-url}")
    private String feedbackURL;

    public void handleOpenFeedbackLink() {
        var hostServices = tWinFileShareApplication.getHostServices();
        hostServices.showDocument(feedbackURL);
    }

    public void handleOpenAboutDialog() {
        try {
            Stage dialog = new Stage();
            Scene scene = TWFSFxApplication.generateScene("/templates/fx/About.fxml");
            dialog.setScene(scene);
            dialog.initModality(Modality.NONE);
            dialog.setMaximized(false);
            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Value("${twfs.contact-url}")
    private String contactURL;

    public void handleOpenContactDevLink() {
        var hostServices = tWinFileShareApplication.getHostServices();
        hostServices.showDocument(contactURL);
    }

    @Value("${twfs.request-access-url}")
    private String requestAccessURL;

    public void handleOpenRequestAccessLink() {
        var hostServices = tWinFileShareApplication.getHostServices();
        hostServices.showDocument(requestAccessURL);
    }
}
