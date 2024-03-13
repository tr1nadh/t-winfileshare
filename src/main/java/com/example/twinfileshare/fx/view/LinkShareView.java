package com.example.twinfileshare.fx.view;

import com.example.twinfileshare.fx.presenter.LinkSharePresenter;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Controller
public class LinkShareView implements ILinkShareView {

    private LinkSharePresenter presenter;
    @FXML
    private ChoiceBox<String> accountChoiceBox;
    @FXML
    private ListView<File> fileListView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        presenter.init();
        accountChoiceBox.valueProperty().addListener(((observableValue, oldValue, newValue) ->
                AccountChoiceBoxValueChanged(newValue)));
    }

    public void AccountChoiceBoxValueChanged(String selectedValue) {
        presenter.handleAccountChoiceBoxValueChanged(selectedValue);
    }

    public void setAccountChoiceBoxItems(List<String> items) {
        accountChoiceBox.setItems(FXCollections.observableArrayList(items));
    }

    @Override
    public void addAccountChoiceBoxItem(String item) {
        accountChoiceBox.getItems().add(item);
    }

    public void setAccountChoiceBoxValue(String value) {
        accountChoiceBox.setValue(value);
    }

    public void removeItemFromAccountChoiceBox(String item) {
        accountChoiceBox.getItems().remove(item);
    }

    public String getAccountChoiceBoxValue() {
        return accountChoiceBox.getValue();
    }

    public boolean accountChoiceBoxContains(String item) {
        return accountChoiceBox.getItems().contains(item);
    }

    public void setFileListViewSelectionMode(SelectionMode selectionMode) {
        fileListView.getSelectionModel().setSelectionMode(selectionMode);
    }

    @Override
    public void addItemsToFileListView(List<File> items) {
        fileListView.getItems().addAll(items);
    }

    public List<File> getFileListViewItems() {
        return fileListView.getItems();
    }

    @Override
    public void removeAllFileListViewItems() {
        fileListView.getItems().setAll(new ArrayList<>());
    }

    @Override
    public boolean isFileListViewEmpty() {
        return fileListView.getItems().isEmpty();
    }

    public void clearFileListViewItems() {
        fileListView.getItems().clear();
    }

    public List<File> getSelectedFileListViewItems() {
        return fileListView.getSelectionModel().getSelectedItems();
    }

    public List<File> openMultipleFileChooserWindow(String windowTitle, ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(windowTitle);

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        return fileChooser.showOpenMultipleDialog(stage);
    }

    public String showTextInputDialog(String placeholderText,
                                      String title,
                                      String header) {
        var text = new TextInputDialog();
        text.setTitle(title);
        text.setHeaderText(header);
        text.getEditor().setPromptText(placeholderText);

        return text.showAndWait().orElse(null);
    }

    public void setUploadPresenter(LinkSharePresenter presenter) {
        this.presenter = presenter;
    }

    public void addFilesFromFileManager(ActionEvent event) {
        presenter.handleAddFilesFilesFromFileManager(event);
    }

    public void removeFilesFromFileListView(ActionEvent event) {
        presenter.handleRemoveFilesFromListView();
    }

    public void clearFileListView(ActionEvent event) {
        presenter.handleClearListView();
    }

    public void uploadFiles(ActionEvent event) throws IOException {
        presenter.handleUploadFiles(event);
    }
}
