package com.example.twinfileshare.fx;

import com.example.twinfileshare.TWinFileShareApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class TWFSFxApplication {

    public static void loadView(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(TWFSFxApplication.class.getResource("/templates/fx/Main.fxml"));
        loader.setControllerFactory(TWinFileShareApplication.getApplicationContext()::getBean);
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
