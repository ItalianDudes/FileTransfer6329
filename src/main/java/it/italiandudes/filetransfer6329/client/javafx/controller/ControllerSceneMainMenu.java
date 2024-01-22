package it.italiandudes.filetransfer6329.client.javafx.controller;

import it.italiandudes.filetransfer6329.client.javafx.Client;
import it.italiandudes.filetransfer6329.client.javafx.alert.ConfirmationAlert;
import it.italiandudes.filetransfer6329.client.javafx.alert.ErrorAlert;
import it.italiandudes.filetransfer6329.client.javafx.alert.InformationAlert;
import it.italiandudes.filetransfer6329.client.javafx.scene.SceneReceiver;
import it.italiandudes.filetransfer6329.client.javafx.scene.SceneSettingsEditor;
import it.italiandudes.filetransfer6329.client.javafx.scene.SceneTransmitter;
import it.italiandudes.filetransfer6329.utils.Defs;
import it.italiandudes.filetransfer6329.utils.Updater;
import it.italiandudes.idl.common.Logger;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public final class ControllerSceneMainMenu {

    // Initialize
    @FXML
    private void initialize() {
        Client.getStage().setResizable(true);
    }

    // EDT
    @FXML
    private void checkForUpdates() {
        new InformationAlert("INFO", "NON IMPLEMENTATO", "Questa funzionalita' non e' stata ancora implementata.");
        /*
        Scene thisScene = Client.getStage().getScene();
        Client.getStage().setScene(SceneLoading.getScene());
        new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() {
                        String latestVersion = Updater.getLatestVersion();
                        if (latestVersion == null) {
                            Platform.runLater(() -> {
                                new ErrorAlert("ERRORE", "Errore di Connessione", "Si e' verificato un errore durante il controllo della versione.");
                                Client.getStage().setScene(thisScene);
                            });
                            return null;
                        }

                        String currentVersion = null;
                        try {
                            Attributes attributes = JarHandler.ManifestReader.readJarManifest(Defs.JAR_POSITION);
                            currentVersion = JarHandler.ManifestReader.getValue(attributes, "Version");
                        } catch (IOException e) {
                            Logger.log(e);
                        }

                        if (Updater.getLatestVersion(currentVersion, latestVersion).equals(currentVersion)) {
                            Platform.runLater(() -> {
                                new InformationAlert("AGGIORNAMENTO", "Controllo Versione", "La versione corrente e' la piu' recente.");
                                Client.getStage().setScene(thisScene);
                            });
                            return null;
                        }

                        String finalCurrentVersion = currentVersion;
                        Platform.runLater(() -> {
                            if (new ConfirmationAlert("AGGIORNAMENTO", "Trovata Nuova Versione", "E' stata trovata una nuova versione. Vuoi scaricarla?\nVersione Corrente: "+ finalCurrentVersion +"\nNuova Versione: "+latestVersion).result) {
                                updateApp(thisScene, latestVersion);
                            } else {
                                Platform.runLater(() -> Client.getStage().setScene(thisScene));
                            }

                        });
                        return null;
                    }
                };
            }
        }.start();*/
    }
    @SuppressWarnings("DuplicatedCode")
    private void updateApp(@NotNull final Scene thisScene, @NotNull final String latestVersion) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Aggiornamento FileTransfer6329");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Java Executable File", "*.jar"));
        fileChooser.setInitialFileName(Defs.APP_FILE_NAME+"-"+latestVersion+".jar");
        fileChooser.setInitialDirectory(new File(Defs.JAR_POSITION).getParentFile());
        File fileNewApp;
        try {
            fileNewApp = fileChooser.showSaveDialog(Client.getStage().getScene().getWindow());
        } catch (IllegalArgumentException e) {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            fileNewApp = fileChooser.showSaveDialog(Client.getStage().getScene().getWindow());
        }
        if (fileNewApp == null) {
            Client.getStage().setScene(thisScene);
            return;
        }
        File finalFileNewApp = fileNewApp;
        new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() {
                        try {
                            Updater.downloadNewVersion(finalFileNewApp.getAbsoluteFile().getParent() + File.separator + Defs.APP_FILE_NAME+"-"+latestVersion+".jar");
                            Platform.runLater(() -> {
                                if (new ConfirmationAlert("AGGIORNAMENTO", "Aggiornamento", "Download della nuova versione completato! Vuoi chiudere questa app?").result) {
                                    System.exit(0);
                                } else {
                                    Client.getStage().setScene(thisScene);
                                }
                            });
                        } catch (IOException e) {
                            Logger.log(e);
                            Platform.runLater(() -> {
                                new ErrorAlert("ERRORE", "Errore di Download", "Si e' verificato un errore durante il download della nuova versione dell'app.");
                                Client.getStage().setScene(thisScene);
                            });
                        }
                        return null;
                    }
                };
            }
        }.start();
    }
    @FXML
    private void openSettingsEditor() {
        Client.getStage().setScene(SceneSettingsEditor.getScene());
    }
    @FXML
    private void openAsTransmitter() {
        Client.getStage().setScene(SceneTransmitter.getScene());
    }
    @FXML
    private void openAsReceiver() {
        Client.getStage().setScene(SceneReceiver.getScene());
    }
}
