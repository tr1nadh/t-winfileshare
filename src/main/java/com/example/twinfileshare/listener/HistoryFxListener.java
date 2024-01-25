package com.example.twinfileshare.listener;

import com.example.twinfileshare.entity.HistoryFile;
import com.example.twinfileshare.event.payload.FileUploadSuccessEvent;
import com.example.twinfileshare.fx.HistoryPresenter;
import com.example.twinfileshare.repository.HistoryRepository;
import javafx.application.Platform;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class HistoryFxListener {

    @Autowired
    private HistoryPresenter historyPresenter;

    @Autowired
    private HistoryRepository historyRepository;

    @EventListener
    public void HandFileUploadSuccess(FileUploadSuccessEvent event) {
        var driveResponse = event.getDriveUploadResponse();
        var filename = driveResponse.getFilename();
        var id = driveResponse.getId();
        var link = driveResponse.getSharableLink();
        var history = new HistoryFile(id, filename, link);
        Platform.runLater( () -> historyPresenter.addFile(history));
        log.info("File '" + filename + "' has been added to history");
        historyRepository.save(history);
    }
}