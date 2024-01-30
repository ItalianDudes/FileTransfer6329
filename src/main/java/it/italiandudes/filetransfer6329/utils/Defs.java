package it.italiandudes.filetransfer6329.utils;

import it.italiandudes.filetransfer6329.FileTransfer6329;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

public final class Defs {

    // Jar App Position
    public static final String JAR_POSITION;
    static {
        try {
            JAR_POSITION = new File(FileTransfer6329.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    // Max HTTPServer Backlog
    public static final int HTTPSERVER_BACKLOG = 10;

    // HTTPServer Stop Delay
    public static final int HTTPSERVER_STOP_DELAY = 0;

    // Resources Location
    @SuppressWarnings("unused")
    public static final class Resources {

        //Resource Getters
        public static URL get(@NotNull final String resourceConst) {
            return Objects.requireNonNull(FileTransfer6329.class.getResource(resourceConst));
        }
        public static InputStream getAsStream(@NotNull final String resourceConst) {
            return Objects.requireNonNull(FileTransfer6329.class.getResourceAsStream(resourceConst));
        }

        // JSON
        public static final class JSON {
            public static final String JSON_CONFIGURATION = "configuration.json";
            public static final String DEFAULT_JSON_CONFIGURATION = "/json/" + JSON_CONFIGURATION;
        }
    }
}
