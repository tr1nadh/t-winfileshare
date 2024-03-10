package com.example.twinfileshare.fx.view;

import com.example.twinfileshare.entity.SharedFile;
import com.example.twinfileshare.fx.presenter.ManagePresenter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Controller
public class ManageView implements Initializable {

    private ManagePresenter presenter;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        presenter.init();
        accountChoiceBox.valueProperty().addListener(((observableValue, oldValue, newValue) ->
                loadSharedFiles(newValue)));
    }

    public void loadSharedFiles(String currentValue) {
        presenter.handleLoadingSharedFiles(currentValue);
    }

    public void copyLinkToClipboard(ActionEvent event) throws InterruptedException {
        presenter.handleCopyLinkToClipboard(event);
    }

    public void stopFileSharing(ActionEvent event) throws IOException {
        presenter.handleStopFileSharing(event);
    }

    public void startFileSharing(ActionEvent event) throws IOException {
        presenter.handleStartFileSharing(event);
    }

    public void deleteFile(ActionEvent event) throws IOException {
        presenter.handleDeleteFile(event);
    }

    @FXML
    private ListView<SharedFile> ManageListView = new ListView<>();

    public SharedFile getSelectedSharedFile() {
        return ManageListView.getSelectionModel().getSelectedItem();
    }

    public void addFileToListView(SharedFile sharedFile) {
        ManageListView.getItems().add(sharedFile);
    }

    public void addFilesToListView(List<SharedFile> sharedFiles) {
        ManageListView.getItems().addAll(sharedFiles);
    }

    public void setItemsToListView(List<SharedFile> sharedFiles) {
        ManageListView.getItems().setAll(sharedFiles);
    }

    public void deleteFileFromListView(SharedFile sharedFile) {
        ManageListView.getItems().remove(sharedFile);
    }

    @FXML
    private ChoiceBox<String> accountChoiceBox;

    public void addItemsToAccountChoiceBox(List<String> items) {
        accountChoiceBox.getItems().addAll(items);
    }

    public void setManagePresenter(ManagePresenter presenter) {
        this.presenter = presenter;
    }

    public int getAccountChoiceBoxIndexOf(String selectedValue) {
        return accountChoiceBox.getItems().indexOf(selectedValue);
    }

    public void setAccountByIndex(int index) {
        accountChoiceBox.getSelectionModel().select(index);
    }

    public void addItemToAccountChoiceBox(String item) {
        accountChoiceBox.getItems().add(item);
    }

    public String getCurrentSelectedAccount() {
        return accountChoiceBox.getSelectionModel().getSelectedItem();
    }

    public void removeItemFromChoiceBox(String item) {
        accountChoiceBox.getItems().remove(item);
    }
}
