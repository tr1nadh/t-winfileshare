package com.example.twinfileshare.fx;

import com.example.twinfileshare.TWinFileShareApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Configuration
@Component
public class TWFSFxApplication {

    private static Stage mainStage;

    public static void loadPrimaryView(Stage stage) throws IOException {
        mainStage = getStageWithProperties(stage);
        mainStage.setScene(generateScene("/templates/fx/Main.fxml"));
        mainStage.show();
    }

    private static String appWindowName;

    @Value("${twfs.window.name}")
    private void setWindowName(String windowName) {
        appWindowName = windowName;
    }

    private static String appIcon;

    @Value("${twfs.window.icon}")
    private void setAppIcon(String icon) {
        appIcon = icon;
    }

    private static Stage getStageWithProperties(Stage stage) {
        stage.setTitle(appWindowName);
        stage.getIcons().add(new Image(appIcon));
        stage.setResizable(false);
        stage.setWidth(800);
        stage.setHeight(600);
        return stage;
    }

    public static Scene generateScene(String resource) throws IOException {
        FXMLLoader loader = new FXMLLoader(TWFSFxApplication.class.getResource(resource));
        loader.setControllerFactory(TWinFileShareApplication.getApplicationContext()::getBean);
        Parent root = loader.load();
        return new Scene(root);
    }

    public static void loadScene(String resource) throws IOException {
        mainStage.setScene(generateScene(resource));
        mainStage.show();
    }
}
