package com.example.twinfileshare.fx.presenter;

import com.example.twinfileshare.TWinFileShareApplication;
import com.example.twinfileshare.fx.view.MainView;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    @Value("${twfs.about-url}")
    private String aboutURL;

    public void handleOpenAboutSite() {
        var hostServices = tWinFileShareApplication.getHostServices();
        hostServices.showDocument(aboutURL);
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
