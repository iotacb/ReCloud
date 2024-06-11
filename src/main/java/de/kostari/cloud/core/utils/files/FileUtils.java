package de.kostari.cloud.core.utils.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtils {

    public static String loadAsString(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

}
