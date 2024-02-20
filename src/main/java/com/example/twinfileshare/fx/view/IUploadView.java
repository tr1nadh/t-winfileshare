package com.example.twinfileshare.fx.view;

import com.example.twinfileshare.fx.presenter.UploadPresenter;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.SelectionMode;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public interface IUploadView extends Initializable {
    void setAccountChoiceBoxItems(List<String> items);
    void addAccountChoiceBoxItem(String item);
    void setAccountChoiceBoxValue(String value);
    void removeItemFromAccountChoiceBox(String item);
    String getAccountChoiceBoxValue();
    void disableAccountChoiceBox(boolean disable);
    boolean accountChoiceBoxContains(String item);
    void disableAccountDisconnectBTN(boolean disable);
    void disableAddFilesBTN(boolean disable);
    void disableRemoveFilesBTN(boolean disable);
    void disableClearFilesBTN(boolean disable);
    void disableHistoryBTN(boolean disable);
    void setFileListViewSelectionMode(SelectionMode selectionMode);
    void addItemsToFileListView(List<String> items);
    ObservableList<String> getFileListViewItems();
    boolean isFileListViewEmpty();
    void clearFileListViewItems();
    ObservableList<String> getSelectedFileListViewItems();
    void disableFileListView(boolean disable);
    void setFileListViewItems(ObservableList<String> items);
    void setFileUploadProgressBarVisible(boolean visible);
    void updateFileUploadProgressBar(double value);
    void setUploadBTNText(String text);
    List<File> openMultipleFileChooserWindow(String windowTitle, ActionEvent event);
    String showTextInputDialog(String placeholderText, String title, String header);
    void setUploadPresenter(UploadPresenter presenter);
    void connectGoogleDrive(ActionEvent event);
    void disconnectSelectedAccount(ActionEvent event);
    void addFilesFromFileManager(ActionEvent event);
    void removeFilesFromFileListView(ActionEvent event);
    void clearFileListView(ActionEvent event);
    void changeToHistoryScene(ActionEvent event) throws IOException;
    void uploadFiles(ActionEvent event) throws IOException, InterruptedException, GeneralSecurityException;
}
