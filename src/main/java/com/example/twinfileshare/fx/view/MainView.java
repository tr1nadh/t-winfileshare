package com.example.twinfileshare.fx.view;

import com.example.twinfileshare.fx.TWFSFxApplication;
import com.example.twinfileshare.fx.presenter.MainPresenter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import lombok.SneakyThrows;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.ResourceBundle;

@Controller
public class MainView implements Initializable {

    @FXML
    private Tab shareVLinkTab;

    @FXML
    private Tab shareVEmailTab;

    @FXML
    private Tab manageTab;

    @FXML
    private AnchorPane manageAccountPane;

    private MainPresenter mainPresenter;


    @SneakyThrows
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        shareVLinkTab.setContent(TWFSFxApplication.generateScene("/templates/fx/LinkShare.fxml").getRoot());
        shareVEmailTab.setContent(TWFSFxApplication.generateScene("/templates/fx/EmailShare.fxml").getRoot());
        manageTab.setContent(TWFSFxApplication.generateScene("/templates/fx/Manage.fxml").getRoot());
        manageAccountPane.getChildren().setAll(TWFSFxApplication.generateScene("/templates/fx/AccountManage.fxml").getRoot());
    }

    public void openFeedbackLink(ActionEvent event) {
        mainPresenter.handleOpenFeedbackLink();
    }

    public void openAboutSite(ActionEvent event) {
        mainPresenter.handleOpenAboutSite();
    }

    public void openContactDevLink(ActionEvent event) {
        mainPresenter.handleOpenContactDevLink();
    }

    public void openRequestAccessLink(ActionEvent event) {
        mainPresenter.handleOpenRequestAccessLink();
    }

    public void setMainPresenter(MainPresenter mainPresenter) {
        this.mainPresenter = mainPresenter;
    }
}
