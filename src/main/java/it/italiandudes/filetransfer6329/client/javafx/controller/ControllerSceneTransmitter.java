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
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static it.italiandudes.filetransfer6329.client.javafx.JFXDefs.IMAGE_OFFLINE;
import static it.italiandudes.filetransfer6329.client.javafx.JFXDefs.IMAGE_ONLINE;

public final class ControllerSceneTransmitter {

    // Attributes
    private boolean isOnline = false;
    private ServerSocket serverSocket = null;
    private ServerListener serverListener = null;
    public static HashMap<Socket, ClientHandler> activeConnections = null;
    private static HashSet<ServerElement> availableFileSet = null;
    private static TableView<ServerElement> staticTableView;

    // Graphic Elements
    @FXML private TableView<ServerElement> tableViewFileList;
    @FXML private TableColumn<ServerElement, Integer> tableColumnID;
    @FXML private TableColumn<ServerElement, String> tableColumnFilename;
    @FXML private TableColumn<ServerElement, String> tableColumnFileAbsolutePath;
    @FXML private TableColumn<ServerElement, Long> tableColumnFileSizeKB;
    @FXML private TextArea textAreaLog;
    private static TextArea staticTextAreaLog = null;
    @FXML private Spinner<Integer> spinnerPort;
    @FXML private ImageView imageViewConnectionStatus;
    @FXML private Button buttonConnectionStatus;

    // Initialize
    @FXML @SuppressWarnings("DuplicatedCode")
    private void initialize() {
        Client.getStage().setResizable(true);
        availableFileSet = new HashSet<>();
        staticTextAreaLog = textAreaLog;
        staticTableView = tableViewFileList;
        spinnerPort.getEditor().setTextFormatter(UIElementConfigurator.configureNewIntegerTextFormatter());
        spinnerPort.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 65535, 21, 1));
        imageViewConnectionStatus.setImage(IMAGE_OFFLINE);
        tableColumnID.setCellValueFactory(new PropertyValueFactory<>("id"));
        tableColumnFilename.setCellValueFactory(new PropertyValueFactory<>("filename"));
        tableColumnFileAbsolutePath.setCellValueFactory(new PropertyValueFactory<>("fileAbsolutePath"));
        tableColumnFileSizeKB.setCellValueFactory(new PropertyValueFactory<>("fileSizeKB"));
    }

    // Methods
    public static void writeMessageOnLog(@NotNull final String message) {
        Platform.runLater(() -> {
            if (staticTextAreaLog != null) {
                staticTextAreaLog.appendText(message + '\n');
            }
        });
    }
    @NotNull
    public static HashSet<ServerElement> getElementList() {
        return new HashSet<>(availableFileSet);
    }
    public static void removeUnavailableElementFromList(@NotNull final ServerElement element) {
        availableFileSet.remove(element);
        Platform.runLater(() -> staticTableView.getItems().remove(element));
    }

    // EDT
    @FXML
    private void addFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona Uno o Piu' File");
        fileChooser.setInitialDirectory(new File(Defs.JAR_POSITION).getParentFile());
        List<File> files;
        try {
            files = fileChooser.showOpenMultipleDialog(Client.getStage().getScene().getWindow());
        } catch (IllegalArgumentException e) {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            files = fileChooser.showOpenMultipleDialog(Client.getStage().getScene().getWindow());
        }
        if (files != null) {
            HashSet<File> fileSet = new HashSet<>(files);
            int total = fileSet.size();
            int success = 0;
            int fail = 0;
            ArrayList<String> failPath = new ArrayList<>();
            for (File file : fileSet) {
                try {
                    ServerElement element = new ServerElement(file);
                    tableViewFileList.getItems().add(element);
                    availableFileSet.add(element);
                    success++;
                } catch (FileNotFoundException e) {
                    fail++;
                    failPath.add(file.getAbsolutePath());
                }
            }
            if (fail > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("Alcuni dei file forniti non esistono o non sono validi.").append('\n');
                sb.append("File Forniti: ").append(total).append('\n');
                sb.append("Successi: ").append(success).append('\n');
                sb.append("Fallimenti: ").append(fail).append('\n');
                for (String s : failPath) {
                    sb.append(s).append('\n');
                }
                new ErrorAlert("ERRORE", "Errore di Inserimento", sb.toString());
            }
        }
    }
    @FXML
    private void removeFile() {
        ServerElement element = tableViewFileList.getSelectionModel().getSelectedItem();
        if (element != null) {
            tableViewFileList.getItems().remove(element);
            availableFileSet.remove(element);
        }
    }
    @FXML
    private void changeConnectionStatus() {
        int port;
        try {
            port = Integer.parseInt(spinnerPort.getEditor().getText());
        } catch (NumberFormatException e) {
            return;
        }
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
                            Set<Socket> connections = activeConnections.keySet();
                            for (Socket connection : connections) {
                                try {
                                    activeConnections.get(connection).interrupt();
                                } catch (NullPointerException ignored) {}
                                activeConnections.remove(connection);
                                try {
                                    connection.close();
                                } catch (Exception ignored) {}
                            }
                            activeConnections.clear();
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
                            //noinspection DuplicatedCode
                            try {
                                serverSocket = new ServerSocket(port);
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
                            serverListener = new ServerListener(serverSocket);
                            serverListener.start();
                            Logger.log("Listening on port: " + port);
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
