package it.italiandudes.bot6329.modules.configuration;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public final class ConfigurationMap {

    // Default Configuration Map
    public static final HashMap<String, Object> DEFAULT_CONFIGURATION = new HashMap<>();
    static {
        DEFAULT_CONFIGURATION.put(Keys.TOKEN, null);
        DEFAULT_CONFIGURATION.put(Keys.DATABASE_PATH, "bot6329.sqlite3");
        DEFAULT_CONFIGURATION.put(Keys.BLACKLIST, new JSONArray());
    }

    public static void fixEntry(@NotNull final JSONObject CONFIGURATION, @NotNull final String KEY) {
        CONFIGURATION.remove(KEY);
        if (KEY.equals(Keys.TOKEN)) {
            CONFIGURATION.put(KEY, JSONObject.NULL);
        } else {
            CONFIGURATION.put(KEY, DEFAULT_CONFIGURATION.get(KEY));
        }
    }

    // Keys
    public static final class Keys {
        public static final String TOKEN = "token";
        public static final String DATABASE_PATH = "database_path";
        public static final String BLACKLIST = "blacklist";
    }
}
