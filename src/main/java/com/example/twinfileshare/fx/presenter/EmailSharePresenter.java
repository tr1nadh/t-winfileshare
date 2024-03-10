package com.example.twinfileshare.fx.presenter;

import com.example.twinfileshare.SendMessage;
import com.example.twinfileshare.fx.TWFSFxApplication;
import com.example.twinfileshare.fx.model.EmailShareModal;
import com.example.twinfileshare.fx.view.EmailShareView;
import jakarta.annotation.PostConstruct;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.IOException;

@Service
public class EmailSharePresenter {

    @Autowired
    private EmailShareView emailShareView;

    @Autowired
    private EmailShareModal emailShareModal;

    @PostConstruct
    public void setView() {
        emailShareView.setEmailSharePresenter(this);
    }

    public void init() {
        emailShareView.setFromAccountChoiceBoxItems(emailShareModal.getAllEmails());
    }

    @Autowired
    private TWFSFxApplication twfsFxApplication;


    public void handleManageAccounts(ActionEvent event) {
        var node = (Node) event.getSource();
        var stage = (Stage) node.getScene().getWindow();
        try {
            Stage dialog = new Stage();
            Scene scene = TWFSFxApplication.generateScene("/templates/fx/AccountManage.fxml");
            dialog.setScene(scene);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(stage);
            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Autowired
    private SendMessage sendMessage;

    @Autowired
    private LinkSharePresenter presenter;

    public void handleSendAndShare() throws MessagingException, IOException {
        var fromEmail = emailShareView.getSelectedFromAccountChoiceBoxItem();
        var toEmail = emailShareView.getToTextValue();
        var subject = emailShareView.getTitleTextValue();
        var body = emailShareView.getMessageBodyTextValue();
        var message = sendMessage.sendEmail(fromEmail,
                toEmail, subject, body, "download-link");

        System.out.println(message);
    }
}
