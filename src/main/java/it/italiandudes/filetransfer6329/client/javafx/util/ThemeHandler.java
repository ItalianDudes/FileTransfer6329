package it.italiandudes.filetransfer6329.client.javafx.util;

import it.italiandudes.filetransfer6329.client.javafx.Client;
import it.italiandudes.filetransfer6329.client.javafx.JFXDefs;
import it.italiandudes.filetransfer6329.utils.Defs;
import javafx.scene.Scene;
import org.jetbrains.annotations.NotNull;

public final class ThemeHandler {

    // Config Theme
    private static String configTheme = null;

    // Methods
    public static void setConfigTheme() {
        if (Client.getSettings().getBoolean(Defs.SettingsKeys.ENABLE_DARK_MODE)) {
            configTheme = Defs.Resources.get(JFXDefs.Resources.CSS.CSS_DARK_THEME).toExternalForm();
        } else {
            configTheme = Defs.Resources.get(JFXDefs.Resources.CSS.CSS_LIGHT_THEME).toExternalForm();
        }
    }

    // Config Theme
    public static void loadConfigTheme(@NotNull final Scene scene) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(configTheme);
    }

    // Light Theme
    public static void loadLightTheme(@NotNull final Scene scene) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(Defs.Resources.get(JFXDefs.Resources.CSS.CSS_LIGHT_THEME).toExternalForm());
    }

    // Dark Theme
    public static void loadDarkTheme(@NotNull final Scene scene) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(Defs.Resources.get(JFXDefs.Resources.CSS.CSS_DARK_THEME).toExternalForm());
    }

}
