package it.italiandudes.filetransfer6329.modules.configuration;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.HashMap;

public final class ConfigurationMap {

    // Default Configuration Map
    public static final HashMap<String, Object> DEFAULT_CONFIGURATION = new HashMap<>();
    static {
        DEFAULT_CONFIGURATION.put(Keys.SERVER_PORT, 80);
        DEFAULT_CONFIGURATION.put(Keys.SERVER_ROOT_DIRECTORY, "download/");
        DEFAULT_CONFIGURATION.put(Keys.SERVER_DOWNLOAD_SPEED_KB, 64 * 1024);
    }

    public static void fixEntry(@NotNull final JSONObject CONFIGURATION, @NotNull final String KEY) {
        CONFIGURATION.remove(KEY);
        CONFIGURATION.put(KEY, DEFAULT_CONFIGURATION.get(KEY));
    }

    // Keys
    public static final class Keys {
        public static final String SERVER_PORT = "port";
        public static final String SERVER_ROOT_DIRECTORY = "root_directory";
        public static final String SERVER_DOWNLOAD_SPEED_KB = "server_max_download_speed_kb";
    }
}
