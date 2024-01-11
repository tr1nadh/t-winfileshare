package com.example.twinfileshare.service.utility;

import com.google.api.client.util.Strings;
import lombok.extern.log4j.Log4j2;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.progress.ProgressMonitor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@Log4j2
public class Zipper {

    private ZipFile currentZip;

    public boolean zipFiles(List<File> files, String zipName) {
        if (files.isEmpty()) {
            log.warn("No files to zip");
            return false;
        }

        if (Strings.isNullOrEmpty(zipName)) {
            log.error("Cannot create zip: zip name is empty or null");
            return false;
        }

        try (var zip = new ZipFile(zipName + ".zip")) {
            this.currentZip = zip;

            for (var file : files) {
                if (file.exists())
                    zip.addFile(file, createZipParameters(file));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    public ProgressMonitor progressMonitor() {
        return currentZip.getProgressMonitor();
    }

    private ZipParameters createZipParameters(File file) {
        var zipParameters = new ZipParameters();
        zipParameters.setFileNameInZip(file.getName());

        return zipParameters;
    }
}
