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
    void setAccountChoiceBoxValue(String value);
    void removeItemFromAccountChoiceBox(String item);
    String getAccountChoiceBoxValue();
    void disableAccountChoiceBox(boolean disable);
    boolean accountChoiceBoxContains(String item);
    void disableAddFilesBTN(boolean disable);
    void disableRemoveFilesBTN(boolean disable);
    void disableClearFilesBTN(boolean disable);
    void setFileListViewSelectionMode(SelectionMode selectionMode);
    void addItemsToFileListView(List<File> items);
    List<File> getFileListViewItems();
    boolean isFileListViewEmpty();
    void clearFileListViewItems();
    List<File> getSelectedFileListViewItems();
    void disableFileListView(boolean disable);
    void setFileListViewItems(List<File> items);
    void setFileUploadProgressBarVisible(boolean visible);
    void updateFileUploadProgressBar(double value);
    void setUploadBTNText(String text);
    List<File> openMultipleFileChooserWindow(String windowTitle, ActionEvent event);
    String showTextInputDialog(String placeholderText, String title, String header);
    void setUploadPresenter(LinkSharePresenter presenter);
    void addFilesFromFileManager(ActionEvent event);
    void removeFilesFromFileListView(ActionEvent event);
    void clearFileListView(ActionEvent event);
    void uploadFiles(ActionEvent event) throws IOException, InterruptedException, GeneralSecurityException;
    void openManageAccountsDialog(ActionEvent event);

    void removeAllFileListViewItems();
}
