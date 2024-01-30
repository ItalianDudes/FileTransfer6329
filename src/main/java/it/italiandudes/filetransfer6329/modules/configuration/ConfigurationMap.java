package it.italiandudes.filetransfer6329.modules.configuration;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.HashMap;

public final class ConfigurationMap {

    // Default Configuration Map
    public static final HashMap<String, Object> DEFAULT_CONFIGURATION = new HashMap<>();
    static {
        DEFAULT_CONFIGURATION.put(Keys.PORT, 80);
        DEFAULT_CONFIGURATION.put(Keys.ROOT_DIRECTORY, "download/");
        DEFAULT_CONFIGURATION.put(Keys.DOWNLOAD_SPEED_KB, 64 * 1024);
        DEFAULT_CONFIGURATION.put(Keys.LOG_SEND_FOR_DOWNLOAD, false);
    }

    public static void fixEntry(@NotNull final JSONObject CONFIGURATION, @NotNull final String KEY) {
        CONFIGURATION.remove(KEY);
        CONFIGURATION.put(KEY, DEFAULT_CONFIGURATION.get(KEY));
    }

    // Keys
    public static final class Keys {
        public static final String PORT = "port";
        public static final String ROOT_DIRECTORY = "root_directory";
        public static final String DOWNLOAD_SPEED_KB = "max_download_speed_kb";
        public static final String LOG_SEND_FOR_DOWNLOAD = "log_send_for_download";
    }
}
