package com.example.twinfileshare.fx.view;

import com.example.twinfileshare.fx.presenter.LinkSharePresenter;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.SelectionMode;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Component
public interface ILinkShareView extends Initializable {
    void setAccountChoiceBoxItems(List<String> items);
    void addAccountChoiceBoxItem(String item);
    void removeItemFromAccountChoiceBox(String item);
    String getAccountChoiceBoxValue();
    boolean accountChoiceBoxContains(String item);
    void setFileListViewSelectionMode(SelectionMode selectionMode);
    void addItemsToFileListView(List<File> items);
    List<File> getFileListViewItems();
    boolean isFileListViewEmpty();
    void clearFileListViewItems();
    List<File> getSelectedFileListViewItems();
    List<File> openMultipleFileChooserWindow(String windowTitle, ActionEvent event);
    String showTextInputDialog(String placeholderText, String title, String header);
    void setUploadPresenter(LinkSharePresenter presenter);
    void addFilesFromFileManager(ActionEvent event);
    void removeFilesFromFileListView(ActionEvent event);
    void clearFileListView(ActionEvent event);
    void uploadFiles(ActionEvent event) throws IOException, InterruptedException, GeneralSecurityException;
    void removeAllFileListViewItems();
    void setAccountChoiceBoxValue(String value);
}
