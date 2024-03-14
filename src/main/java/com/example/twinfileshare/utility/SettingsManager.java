package com.example.twinfileshare.utility;

import com.google.gson.Gson;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@Service
public class SettingsManager {

    private final Gson gson = new Gson();

    public void writeSettings(Settings settings) {
        FileWriter writer;
        try {
            writer = new FileWriter("settings.json");
            gson.toJson(settings, writer);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Settings readSettings() {
        FileReader reader;
        Settings settings;
        try {
            reader = new FileReader("settings.json");
            settings = gson.fromJson(reader, Settings.class);
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return settings;
    }
}
