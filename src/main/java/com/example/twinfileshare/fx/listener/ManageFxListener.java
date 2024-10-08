package com.example.twinfileshare.fx.listener;

import com.example.twinfileshare.entity.SharedFile;
import com.example.twinfileshare.event.payload.FileUploadSuccessEvent;
import com.example.twinfileshare.event.payload.SelectedAccountChanged;
import com.example.twinfileshare.fx.presenter.ManagePresenter;
import com.example.twinfileshare.repository.ManageRepository;
import javafx.application.Platform;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class ManageFxListener {

    @Autowired
    private ManagePresenter managePresenter;

    @Autowired
    private ManageRepository manageRepository;

    @EventListener
    public void listenFileUploadSuccess(FileUploadSuccessEvent event) {
        var driveResponse = event.getDriveUploadResponse();
        var filename = driveResponse.getFilename();
        var id = driveResponse.getId();
        var link = driveResponse.getSharableLink();
        var email = driveResponse.getEmail();
        var sharedFile = new SharedFile(id, filename, link, email);
        Platform.runLater( () -> addFileToSharedList(sharedFile));
        log.info("File '" + filename + "' has been added to history");
        manageRepository.save(sharedFile);
    }

    private void addFileToSharedList(SharedFile sharedFile) {
        var accountEmail = managePresenter.getCurrentSelectedAccount();
        if (sharedFile.getEmail().equals(accountEmail))
            managePresenter.addFile(sharedFile);
    }

    @EventListener
    public void listenAccountChoiceBoxValueChanged(SelectedAccountChanged accountChanged) {
        managePresenter.loadAccountSharedFiles(accountChanged.getSelectedValue());
    }
}