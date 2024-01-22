package it.italiandudes.filetransfer6329.client.javafx.controller;

import it.italiandudes.filetransfer6329.client.javafx.Client;
import it.italiandudes.filetransfer6329.client.javafx.alert.ErrorAlert;
import it.italiandudes.filetransfer6329.client.javafx.data.ServerElement;
import it.italiandudes.filetransfer6329.client.javafx.scene.SceneMainMenu;
import it.italiandudes.filetransfer6329.client.javafx.socket.transmitter.ClientHandler;
import it.italiandudes.filetransfer6329.client.javafx.socket.transmitter.ServerListener;
import it.italiandudes.filetransfer6329.client.javafx.util.UIElementConfigurator;
import it.italiandudes.filetransfer6329.utils.Defs;
import it.italiandudes.idl.common.Logger;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public final class ControllerSceneTransmitter {

    // Attributes
    private static final Image IMAGE_ONLINE = new Image(Defs.Resources.getAsStream(Defs.Resources.Image.IMAGE_ONLINE));
    private static final Image IMAGE_OFFLINE = new Image(Defs.Resources.getAsStream(Defs.Resources.Image.IMAGE_OFFLINE));
    private boolean isOnline = false;
    private ServerSocket serverSocket = null;
    private ServerListener serverListener = null;
    private HashMap<Socket, ClientHandler> activeConnections = null;

    // Graphic Elements
    @FXML private TableView<ServerElement> tableViewFileList;
    @FXML private TableColumn<ServerElement, String> tableColumnFilename;
    @FXML private TableColumn<ServerElement, String> tableColumnFileAbsolutePath;
    @FXML private TableColumn<ServerElement, Long> tableColumnFileSizeKB;
    @FXML private TextArea textAreaLog;
    @FXML private Spinner<Integer> spinnerPort;
    @FXML private ImageView imageViewConnectionStatus;
    @FXML private Button buttonConnectionStatus;

    // Initialize
    @FXML
    private void initialize() {
        Client.getStage().setResizable(true);
        spinnerPort.getEditor().setTextFormatter(UIElementConfigurator.configureNewIntegerTextFormatter());
        spinnerPort.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 65535, 21, 1));
        imageViewConnectionStatus.setImage(IMAGE_OFFLINE);
        tableColumnFilename.setCellValueFactory(new PropertyValueFactory<>("filename"));
        tableColumnFileAbsolutePath.setCellValueFactory(new PropertyValueFactory<>("fileAbsolutePath"));
        tableColumnFileSizeKB.setCellValueFactory(new PropertyValueFactory<>("fileSizeKB"));
    }

    // EDT
    @FXML
    private void addFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona un File");
        fileChooser.setInitialDirectory(new File(Defs.JAR_POSITION).getParentFile());
        File filePath;
        try {
            filePath = fileChooser.showOpenDialog(Client.getStage().getScene().getWindow());
        } catch (IllegalArgumentException e) {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            filePath = fileChooser.showOpenDialog(Client.getStage().getScene().getWindow());
        }
        if(filePath!=null) {
            try {
                tableViewFileList.getItems().add(new ServerElement(filePath));
            } catch (FileNotFoundException e) {
                new ErrorAlert("ERRORE", "Errore di Percorso", "Il percorso \"" + filePath.getAbsolutePath() + "\" non conduce a un file valido.");
            }
        }
    }
    @FXML
    private void deleteFile() {}
    @FXML
    private void refreshList() {}
    @FXML
    private void changeConnectionStatus() {
        buttonConnectionStatus.setDisable(true);
        if (isOnline) {
            new Service<Void>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<Void>() {
                        @Override
                        protected Void call() {
                            try {
                                serverListener.interrupt();
                                serverSocket.close();
                            } catch (IOException e) {
                                Logger.log(e);
                                Platform.runLater(() -> new ErrorAlert("ERRORE", "Errore di I/O", "Si e' verificato un errore durante la disconnessione. Controlla i log per ulteriori informazioni."));
                            }
                            serverListener = null;
                            activeConnections = null;
                            Platform.runLater(() -> {
                                isOnline = false;
                                buttonConnectionStatus.setText("VAI ONLINE");
                                imageViewConnectionStatus.setImage(IMAGE_OFFLINE);
                                spinnerPort.setDisable(false);
                                buttonConnectionStatus.setDisable(false);
                            });
                            return null;
                        }
                    };
                }
            }.start();
        } else {
            spinnerPort.setDisable(true);
            new Service<Void>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<Void>() {
                        @Override
                        protected Void call() {
                            try {
                                Thread.sleep(5000);
                            } catch (Exception ignored) {}
                            try {
                                serverSocket = new ServerSocket(spinnerPort.getValue());
                            } catch (IllegalArgumentException iae) {
                                Logger.log(iae);
                                Platform.runLater(() -> {
                                    serverSocket = null;
                                    new ErrorAlert("ERRORE", "Errore di Inserimento", "La porta fornita non e' valida.\nInserire una porta compresa tra 0 e 65535 (estremi inclusi).\nRicorda: la porta deve essere aperta sia sul firewall del dispositivo che sul firewall del tuo router!");
                                    spinnerPort.setDisable(false);
                                    buttonConnectionStatus.setDisable(false);
                                });
                                return null;
                            } catch (IOException e) {
                                Logger.log(e);
                                Platform.runLater(() -> {
                                    serverSocket = null;
                                    new ErrorAlert("ERRORE", "Errore di I/O", "Si e' verificato un errore di I/O. La connessione non può essere aperta.");
                                    spinnerPort.setDisable(false);
                                    buttonConnectionStatus.setDisable(false);
                                });
                                return null;
                            }
                            activeConnections = new HashMap<>();
                            serverListener = new ServerListener(serverSocket, activeConnections);
                            serverListener.start();
                            Platform.runLater(() -> {
                                isOnline = true;
                                buttonConnectionStatus.setText("VAI OFFLINE");
                                imageViewConnectionStatus.setImage(IMAGE_ONLINE);
                                buttonConnectionStatus.setDisable(false);
                            });
                            return null;
                        }
                    };
                }
            }.start();
        }
    }
    @FXML
    private void backToMenu() {
        Client.getStage().setScene(SceneMainMenu.getScene());
    }
}
