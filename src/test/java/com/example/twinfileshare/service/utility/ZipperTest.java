package com.example.twinfileshare.service.utility;

import com.example.twinfileshare.utility.Zipper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ZipperTest {


    @Autowired
    private Zipper zipper;

    @Test
    void testIfEmptyFileListToZip() {
        var result = zipper.zipFiles(List.of(), "");

        assertFalse(result);
    }

    @Test
    void testAddingListOfFiles() {
        List<File> files = List.of(
                new File("testfile-path"),
                new File("testfile-path"),
                new File("testfile-path")
        );

        var result = zipper.zipFiles(files, "test-zip");

        assertTrue(result);
    }
}