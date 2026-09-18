package com.example.eventhub.commons;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Component
public class FileUtils {
    @Autowired
    private ResourceLoader resourceLoader;

    public String readResourceFile(String fileName) throws IOException {
        var file = resourceLoader.getResource("classpath:%s".formatted(fileName)).getFile();
        return new String(Files.readAllBytes(file.toPath()));
    }

    public File getResourceAsFile(String fileName) throws IOException {
        return resourceLoader.getResource("classpath:%s".formatted(fileName)).getFile();
    }
}
