package com.example.twinfileshare.fx.view;

import com.example.twinfileshare.fx.TWFSFxApplication;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
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

    @SneakyThrows
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        shareVLinkTab.setContent(TWFSFxApplication.generateScene("/templates/fx/LinkShare.fxml").getRoot());
        shareVEmailTab.setContent(TWFSFxApplication.generateScene("/templates/fx/LinkShare.fxml").getRoot());
        manageTab.setContent(TWFSFxApplication.generateScene("/templates/fx/Manage.fxml").getRoot());
    }
}
