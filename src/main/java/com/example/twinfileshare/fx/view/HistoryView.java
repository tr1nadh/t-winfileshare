package com.example.twinfileshare.fx.view;

import com.example.twinfileshare.fx.HistoryPresenter;
import javafx.event.ActionEvent;
import lombok.Setter;
import org.springframework.stereotype.Controller;

import java.io.IOException;

@Setter
@Controller
public class HistoryView {

    private HistoryPresenter presenter;

    public void changeToMainScene(ActionEvent event) throws IOException {
        presenter.HandleChangeToMainScene();
    }
}
