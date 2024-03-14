package com.example.twinfileshare.fx.presenter;

import com.example.twinfileshare.TWinFileShareApplication;
import com.example.twinfileshare.fx.TWFSFxApplication;
import com.example.twinfileshare.fx.alert.FxAlert;
import com.example.twinfileshare.fx.view.MainView;
import com.example.twinfileshare.repository.GoogleUserCREDRepository;
import com.example.twinfileshare.utility.SettingsManager;
import jakarta.annotation.PostConstruct;
import javafx.application.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MainPresenter {

    @Autowired
    private MainView mainView;

    public MainPresenter() {
        TWFSFxApplication.startup(this::showRequestAccessAlert);
    }

    @PostConstruct
    public void setMainView() {
        mainView.setMainPresenter(this);
    }

    @Autowired
    private TWinFileShareApplication tWinFileShareApplication;

    @Value("${twfs.feedback-url}")
    private String feedbackURL;

    public void handleOpenFeedbackSite() {
        var hostServices = tWinFileShareApplication.getHostServices();
        hostServices.showDocument(feedbackURL);
    }

    @Value("${twfs.about-url}")
    private String aboutURL;

    public void handleOpenAboutSite() {
        var hostServices = tWinFileShareApplication.getHostServices();
        hostServices.showDocument(aboutURL);
    }

    @Value("${twfs.request-access-url}")
    private String requestAccessURL;

    public void handleOpenRequestAccessSite() {
        var hostServices = tWinFileShareApplication.getHostServices();
        hostServices.showDocument(requestAccessURL);
    }

    @Value("${twfs.contact-url}")
    private String contactURL;

    public void handleOpenContactSite() {
        var hostServices = tWinFileShareApplication.getHostServices();
        hostServices.showDocument(contactURL);
    }

    public void handleCloseTheApplication() {
        Platform.exit();
    }

    @Autowired
    private FxAlert fxAlert;

    @Autowired
    private SettingsManager settingsManager;

    @Autowired
    private GoogleUserCREDRepository googleUserCREDRepository;

    public void showRequestAccessAlert() {
        var settings = settingsManager.readSettings();
        if (settings.isHasAccess())
            return;

        fxAlert.informationAlert(
                "Request alert",
                "As the application is in beta stage, " +
                        "you are required to request access in help menu!!"
        );

        var emails = googleUserCREDRepository.getAllEmails();
        if (!emails.isEmpty()) {
            settings.setHasAccess(true);
            settingsManager.writeSettings(settings);
        }
    }
}
