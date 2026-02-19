package org.enginecraft.util;

import org.enginecraft.objects.DataDictionary;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WriteUtil {
    public static void writeFile(Path outputPath, DataDictionary dict) throws IOException {
        if (dict == null || dict.data.isEmpty()) return;

        Files.createDirectories(outputPath.getParent());
        try (BufferedWriter bw = Files.newBufferedWriter(outputPath)) {
            for (String[] row : dict.data) {
                bw.write(String.join("\t", row));
                bw.newLine();
            }
        }
    }

    public static void writeFile(Path outputPath, String data) throws IOException {
        Files.createDirectories(outputPath.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            writer.write(data);
        }
    }
}
