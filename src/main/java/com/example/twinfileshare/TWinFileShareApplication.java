package com.example.twinfileshare;

import com.example.twinfileshare.fx.TWFSFxApplication;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.Getter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class TWinFileShareApplication extends Application {

	@Getter
	private static ConfigurableApplicationContext applicationContext;

	@Override
	public void init() {
		applicationContext = SpringApplication.run(TWinFileShareApplication.class);
	}

	@Override
	public void stop() {
		applicationContext.close();
		Platform.exit();
	}

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		TWFSFxApplication.loadView(stage);
	}
}
