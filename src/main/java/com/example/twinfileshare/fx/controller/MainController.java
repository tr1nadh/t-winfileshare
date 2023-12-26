package com.example.twinfileshare.fx.controller;

import com.example.twinfileshare.TWinFileShareApplication;
import com.example.twinfileshare.event.payload.DoubleEmailConnectEvent;
import com.example.twinfileshare.event.payload.NoDriveAccessEvent;
import com.example.twinfileshare.event.payload.UserConnectedEvent;
import com.example.twinfileshare.fx.service.MainService;
import com.example.twinfileshare.repository.GoogleUserCREDRepository;
import com.example.twinfileshare.service.GoogleAuthorizationService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.ResourceBundle;

@Controller
public class MainController implements Initializable {

    @Autowired
    private TWinFileShareApplication tWinFileShareApplication;

    @Autowired
    private MainService mainService;

    public void connectGoogleDrive(ActionEvent event) {
        var alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setResizable(false);
        alert.setTitle("Google drive authorization");
        alert.setHeaderText("Check the google drive box when giving permissions \n" +
                " in consent screen to give access.");
        alert.setContentText("Press OK to open the link in default browser.");
        alert.showAndWait()
                .filter(res -> res == ButtonType.OK)
                .ifPresent(res -> openAuthLinkInDefaultBrowser());
    }

    private void openAuthLinkInDefaultBrowser() {
        var hostServices = tWinFileShareApplication.getHostServices();
        hostServices.showDocument(mainService.getGoogleSignInURL());
    }

    @FXML
    private ChoiceBox<String> accountChoiceBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        accountChoiceBox.getItems().addAll(mainService.getAllEmails());
    }

    public void disconnectSelectedAccount() {
        var currentSelectedEmail = accountChoiceBox.getValue();
        var alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Disconnect Account");
        alert.setHeaderText("Are sure you want to disconnect the google drive \n" +
                "account with email: " + currentSelectedEmail);
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        alert.showAndWait()
                        .filter(res -> res == ButtonType.YES)
                .ifPresent(res -> {
                    try {
                        disconnectAccount(currentSelectedEmail);
                    } catch (GeneralSecurityException | IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        alert.setResizable(false);

    }

    private void disconnectAccount(String currentSelectedEmail) throws GeneralSecurityException, IOException {
        mainService.disconnectAccount(currentSelectedEmail);
        removeItemFromChoiceBox(currentSelectedEmail);
    }

    private void removeItemFromChoiceBox(String currentSelectedEmail) {
        accountChoiceBox.setValue("Select an email");
        accountChoiceBox.getItems().remove(currentSelectedEmail);
    }

    @FXML
    private ListView<String> listViewFiles;

    private final ObservableList<String> fileList = FXCollections.observableArrayList();

    public void openFileManager(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a File");

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);

        for (var file : selectedFiles) {
            if (file != null) {
                fileList.add(file.getName());
            }
        }
        listViewFiles.setItems(fileList);
    }

    public void removeFile(ActionEvent event) {
        var selectedItems = listViewFiles.getSelectionModel().getSelectedItems();
        for (var items : selectedItems) {
            System.out.println(items);
        }
    }

    @Component
    public class UserConnectedListener {

        @EventListener
        public void handleUserConnectedEvent(UserConnectedEvent event) {
            var email = event.getEmail();
            if (!accountChoiceBox.getItems().contains(email))
                accountChoiceBox.getItems().add(email);

            Platform.runLater(() -> {
                var successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Authorization successful");
                successAlert.setResizable(false);
                successAlert.setHeaderText("Google drive successfully connected");
                successAlert.showAndWait();
            });
        }

        @EventListener
        public void handleNoDriveAccessEvent(NoDriveAccessEvent event) {
            Platform.runLater( () -> {
                var noDriveAccessAlert = new Alert(Alert.AlertType.WARNING);
                noDriveAccessAlert.setTitle("Authorization unsuccessful");
                noDriveAccessAlert.setResizable(false);
                noDriveAccessAlert.setHeaderText("Google drive access required!");
                noDriveAccessAlert.setContentText("Press OK to give google drive access.");
                noDriveAccessAlert.getButtonTypes().add(ButtonType.CANCEL);
                noDriveAccessAlert.showAndWait()
                        .filter(res -> res == ButtonType.OK)
                        .ifPresent(res -> openAuthLinkInDefaultBrowser());
            });
        }

        @Autowired
        private GoogleUserCREDRepository googleUserCREDRepository;

        @EventListener
        public void doubleEmailConnectListener(DoubleEmailConnectEvent event) {
            Platform.runLater(
                    () -> {
                        var doubleEmailAlert = new Alert(Alert.AlertType.ERROR);
                        doubleEmailAlert.setTitle("Double email");
                        doubleEmailAlert.setResizable(false);
                        doubleEmailAlert.setHeaderText(event.getMessage());
                        doubleEmailAlert.setContentText("Do you want to change it to current email: "
                                + event.getCurrentEmail());
                        doubleEmailAlert.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
                        doubleEmailAlert.showAndWait()
                                .filter(res -> res == ButtonType.YES)
                                .ifPresent(res -> {
                                    var dbAccount = event.getGoogleUserCRED();
                                    dbAccount.setEmail(event.getCurrentEmail());
                                    googleUserCREDRepository.save(dbAccount);
                                });
                    }
            );
        }
    }
}
