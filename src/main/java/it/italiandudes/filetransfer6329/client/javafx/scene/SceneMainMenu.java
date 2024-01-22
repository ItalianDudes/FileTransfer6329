package it.italiandudes.filetransfer6329.client.javafx.scene;

import it.italiandudes.filetransfer6329.client.javafx.JFXDefs;
import it.italiandudes.filetransfer6329.client.javafx.util.ThemeHandler;
import it.italiandudes.filetransfer6329.utils.Defs;
import it.italiandudes.idl.common.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import java.io.IOException;

public final class SceneMainMenu {

    //Scene Generator
    public static Scene getScene(){
        try {
            Scene scene = new Scene(FXMLLoader.load(Defs.Resources.get(JFXDefs.Resources.FXML.FXML_MAIN_MENU)));
            ThemeHandler.loadConfigTheme(scene);
            return scene;
        }catch (IOException e){
            Logger.log(e);
            return null;
        }
    }
}
