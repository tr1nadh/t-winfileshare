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
}
