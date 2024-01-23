package it.italiandudes.filetransfer6329.client.javafx;

import it.italiandudes.filetransfer6329.utils.Defs;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.stage.Screen;

@SuppressWarnings("unused")
public final class JFXDefs {

    // App Info
    public static final class AppInfo {
        public static final String NAME = "FileTransfer6329";
        public static final Image LOGO = new Image(Defs.Resources.get(Resources.Image.IMAGE_LOGO).toString());
    }

    // System Info
    public static final class SystemGraphicInfo {
        public static final Rectangle2D SCREEN_RESOLUTION = Screen.getPrimary().getBounds();
        public static final double SCREEN_WIDTH = SCREEN_RESOLUTION.getWidth();
        public static final double SCREEN_HEIGHT = SCREEN_RESOLUTION.getHeight();
    }

    // Preloaded Images
    public static final Image IMAGE_ONLINE = new Image(Defs.Resources.getAsStream(Defs.Resources.Image.IMAGE_ONLINE));
    public static final Image IMAGE_OFFLINE = new Image(Defs.Resources.getAsStream(Defs.Resources.Image.IMAGE_OFFLINE));

    // Resource Locations
    public static final class Resources {

        // FXML Location
        public static final class FXML {
            private static final String FXML_DIR = "/fxml/";
            public static final String FXML_LOADING = FXML_DIR + "SceneLoading.fxml";
            public static final String FXML_MAIN_MENU = FXML_DIR + "SceneMainMenu.fxml";
            public static final String FXML_SETTINGS_EDITOR = FXML_DIR + "SceneSettingsEditor.fxml";
            public static final String FXML_TRANSMITTER = FXML_DIR + "SceneTransmitter.fxml";
            public static final String FXML_RECEIVER = FXML_DIR + "SceneReceiver.fxml";
            public static final String FXML_DOWNLOAD_PROGRESS_BAR = FXML_DIR + "SceneDownloadProgressBar.fxml";
        }

        // GIF Location
        public static final class GIF {
            private static final String GIF_DIR = "/gif/";
            public static final String GIF_LOADING = GIF_DIR+"loading.gif";
        }

        // CSS Location
        public static final class CSS {
            private static final String CSS_DIR = "/css/";
            public static final String CSS_LIGHT_THEME = CSS_DIR + "light_theme.css";
            public static final String CSS_DARK_THEME = CSS_DIR + "dark_theme.css";
        }

        // Image Location
        public static final class Image {
            private static final String IMAGE_DIR = "/image/";
            public static final String IMAGE_LOGO = IMAGE_DIR+"app-logo.png";
            public static final String IMAGE_FILE_EXPLORER = IMAGE_DIR+"file-explorer.png";
        }

    }

}
