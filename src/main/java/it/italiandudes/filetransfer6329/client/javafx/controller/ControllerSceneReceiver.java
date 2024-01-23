package it.italiandudes.filetransfer6329.client.javafx.controller;

import it.italiandudes.filetransfer6329.client.javafx.Client;
import it.italiandudes.filetransfer6329.client.javafx.alert.ErrorAlert;
import it.italiandudes.filetransfer6329.client.javafx.alert.InformationAlert;
import it.italiandudes.filetransfer6329.client.javafx.data.ClientElement;
import it.italiandudes.filetransfer6329.client.javafx.scene.SceneDownloadProgressBar;
import it.italiandudes.filetransfer6329.client.javafx.scene.SceneMainMenu;
import it.italiandudes.filetransfer6329.client.javafx.socket.SocketProtocol;
import it.italiandudes.filetransfer6329.client.javafx.util.UIElementConfigurator;
import it.italiandudes.filetransfer6329.utils.Defs;
import it.italiandudes.idl.common.Logger;
import it.italiandudes.idl.common.RawSerializer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

import static it.italiandudes.filetransfer6329.client.javafx.JFXDefs.IMAGE_OFFLINE;
import static it.italiandudes.filetransfer6329.client.javafx.JFXDefs.IMAGE_ONLINE;

public final class ControllerSceneReceiver {

    // Attributes
    private Socket connection = null;
    private static long filesize = 0;
    private static long receivedBytes = 0;
    private static volatile boolean stageShowed = false;
    private static boolean downloadCanceled = false;

    // Methods
    public static long getTotalBytes() {
        return filesize;
    }
    public static long getCurrentBytes() {
        return receivedBytes;
    }
    public static void setDownloadCanceled() {
        downloadCanceled = true;
    }
    public static void setStageShowed() {
        stageShowed = true;
    }

    // Graphic Elements
    @FXML private TableView<ClientElement> tableViewFileList;
    @FXML private TableColumn<ClientElement, Integer> tableColumnID;
    @FXML private TableColumn<ClientElement, String> tableColumnFilename;
    @FXML private TableColumn<ClientElement, Long> tableColumnFileSizeKB;
    @FXML private TextArea textAreaLog;
    @FXML private TextField textFieldAddress;
    @FXML private Spinner<Integer> spinnerPort;
    @FXML private ImageView imageViewConnectionStatus;
    @FXML private Button buttonConnectionStatus;
    @FXML private Button buttonDownloadFile;
    @FXML private Button buttonRefreshFileList;

    // Initialize
    @FXML @SuppressWarnings("DuplicatedCode")
    private void initialize() {
        Client.getStage().setResizable(true);
        spinnerPort.getEditor().setTextFormatter(UIElementConfigurator.configureNewIntegerTextFormatter());
        spinnerPort.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 65535, 21, 1));
        imageViewConnectionStatus.setImage(IMAGE_OFFLINE);
        tableColumnID.setCellValueFactory(new PropertyValueFactory<>("id"));
        tableColumnFilename.setCellValueFactory(new PropertyValueFactory<>("filename"));
        tableColumnFileSizeKB.setCellValueFactory(new PropertyValueFactory<>("fileSizeKB"));
    }

    // EDT
    @FXML
    private void downloadFile() {
        if (connection == null) return;
        ClientElement element = tableViewFileList.getSelectionModel().getSelectedItem();
        if (element == null) return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona la Destinazione");
        fileChooser.setInitialDirectory(new File(Defs.JAR_POSITION).getParentFile());
        fileChooser.setInitialFileName(element.getFilename());
        File file;
        try {
            file = fileChooser.showSaveDialog(Client.getStage().getScene().getWindow());
        } catch (IllegalArgumentException e) {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            file = fileChooser.showSaveDialog(Client.getStage().getScene().getWindow());
        }
        if (file == null) return;
        File finalFile = file;
        receivedBytes = 0;
        filesize = 0;
        downloadCanceled = false;
        buttonDownloadFile.setDisable(true);
        new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() {
                        try {
                            RawSerializer.sendInt(connection.getOutputStream(), SocketProtocol.getIntByRequest(SocketProtocol.DOWNLOAD));
                            RawSerializer.sendInt(connection.getOutputStream(), element.getId());
                            int state = RawSerializer.receiveInt(connection.getInputStream());
                            SocketProtocol protocol = SocketProtocol.getRequestByInt(state);
                            if (protocol == null) {
                                throw new IOException("Protocol received is null.");
                            }
                            switch (protocol) {
                                case UNEXPECTED_VALUE:
                                    Platform.runLater(() -> new ErrorAlert("ERRORE", "Errore di ID", "L'elemento selezionato ha un ID minore di 0. Questo non è possibile."));
                                    break;
                                case ID_NOT_AVAILABLE:
                                    Platform.runLater(() -> new ErrorAlert("ERRORE", "Errore di ID", "L'elemento con questo ID non esiste."));
                                    break;
                                case FILE_NO_MORE_AVAILABLE:
                                    Platform.runLater(() -> {
                                        new ErrorAlert("ERORRE", "Errore di Server", "Il contenuto richiesto non e' piu' disponibile.");
                                        refreshFileList();
                                    });
                                    break;
                                case DOWNLOADING:
                                    filesize = RawSerializer.receiveLong(connection.getInputStream());
                                    receivedBytes = 0;
                                    stageShowed = false;
                                    Platform.runLater(() -> {
                                        Stage popupStage = Client.initPopupStage(SceneDownloadProgressBar.getScene());
                                        popupStage.showAndWait();
                                    });
                                    //noinspection StatementWithEmptyBody
                                    while (!stageShowed);
                                    byte[] buffer = new byte[Defs.BYTE_ARRAY_MAX_SIZE];
                                    try (FileOutputStream fileWriter = new FileOutputStream(finalFile)) {
                                        while (receivedBytes < filesize) {
                                            int expectedBytes = RawSerializer.receiveInt(connection.getInputStream());
                                            int bytesRead = connection.getInputStream().read(buffer, 0, expectedBytes);
                                            if (expectedBytes != bytesRead) {
                                                throw new IOException("Bytes mismatch: expected " + expectedBytes + ", received " + bytesRead);
                                            }
                                            fileWriter.write(buffer, 0, bytesRead);
                                            fileWriter.flush();
                                            if (downloadCanceled) {
                                                RawSerializer.sendInt(connection.getOutputStream(), SocketProtocol.getIntByRequest(SocketProtocol.DOWNLOAD_CANCELED));
                                                break;
                                            } else {
                                                RawSerializer.sendInt(connection.getOutputStream(), SocketProtocol.getIntByRequest(SocketProtocol.OK));
                                            }
                                        }
                                        if (downloadCanceled) {
                                            fileWriter.close();
                                            //noinspection ResultOfMethodCallIgnored
                                            finalFile.delete();
                                            Platform.runLater(() -> new InformationAlert("ANNULLAMENTO", "Download Annullato", "Il download e' stato annullato con successo."));
                                        } else {
                                            int completeDownload = RawSerializer.receiveInt(connection.getInputStream());
                                            if (SocketProtocol.DOWNLOAD_COMPLETE == SocketProtocol.getRequestByInt(completeDownload)) {
                                                Platform.runLater(() -> new InformationAlert("SUCCESSO", "Download Completato", "Il download e' stato completato. Il file si trova in \"" + finalFile.getAbsolutePath() + "\""));
                                            } else {
                                                Logger.log("STATE RECEIVED: " + completeDownload);
                                                throw new IOException("Download failed: confirm not arrived");
                                            }
                                        }
                                        receivedBytes = 0;
                                        filesize = 0;
                                        downloadCanceled = false;
                                        stageShowed = false;
                                    } catch (FileNotFoundException e) {
                                        throw new IOException(e);
                                    }
                                    break;
                                default:
                                    throw new IOException("Protocol not respected");
                            }
                        } catch (IOException e) {
                            Logger.log(e);
                            Platform.runLater(() -> {
                                new ErrorAlert("ERRORE", "Errore di Download", "Si e' verificato un errore durante il download. La connessione e' stata terminata.");
                                buttonDownloadFile.setDisable(true);
                            });
                            disconnect();
                        }
                        return null;
                    }
                };
            }
        }.start();
    }
    @FXML
    private void refreshFileList() {
        if (connection == null) return;
        new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() {
                        try {
                            RawSerializer.sendInt(connection.getOutputStream(), SocketProtocol.getIntByRequest(SocketProtocol.LIST));
                            String base64list = RawSerializer.receiveString(connection.getInputStream());
                            String jsonList = new String(Base64.getDecoder().decode(base64list), StandardCharsets.UTF_8);
                            JSONArray array = new JSONArray(jsonList);
                            ArrayList<ClientElement> elements = new ArrayList<>();
                            for (int i=0; i<array.length(); i++) {
                                JSONObject fileJSON = array.getJSONObject(i);
                                elements.add(new ClientElement(fileJSON));
                            }
                            Platform.runLater(() -> tableViewFileList.setItems(FXCollections.observableList(elements)));
                        } catch (IOException e) {
                            Logger.log(e);
                            disconnect();
                        }
                        return null;
                    }
                };
            }
        }.start();
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
        if (connection != null) {
            new Service<Void>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<Void>() {
                        @Override
                        protected Void call() {
                            disconnect();
                            return null;
                        }
                    };
                }
            }.start();
        } else {
            spinnerPort.setDisable(true);
            textFieldAddress.setDisable(true);
            new Service<Void>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<Void>() {
                        @Override
                        protected Void call() {
                            try {
                                connection = new Socket(textFieldAddress.getText(), port);
                                connection.setSendBufferSize(Defs.BYTE_ARRAY_MAX_SIZE * 2);
                                connection.setReceiveBufferSize(Defs.BYTE_ARRAY_MAX_SIZE * 2);
                            } catch (UnknownHostException uhe) {
                                Logger.log(uhe);
                                Platform.runLater(() -> {
                                    connection = null;
                                    new ErrorAlert("ERRORE", "Errore di Risoluzione", "Risoluzione nome host fallita! Controlla che l'indirizzo fornito sia corretto.");
                                    spinnerPort.setDisable(false);
                                    textFieldAddress.setDisable(false);
                                    buttonConnectionStatus.setDisable(false);
                                });
                            } catch (IllegalArgumentException iae) {
                                Logger.log(iae);
                                Platform.runLater(() -> {
                                    connection = null;
                                    new ErrorAlert("ERRORE", "Errore di Inserimento", "La porta fornita non e' valida.\nInserire una porta compresa tra 0 e 65535 (estremi inclusi).\nRicorda: la porta deve essere aperta sia sul firewall del dispositivo che sul firewall del tuo router!");
                                    spinnerPort.setDisable(false);
                                    textFieldAddress.setDisable(false);
                                    buttonConnectionStatus.setDisable(false);
                                });
                                return null;
                            } catch (IOException e) {
                                try {
                                    connection.close();
                                } catch (Exception ignored) {}
                                Logger.log(e);
                                Platform.runLater(() -> {
                                    connection = null;
                                    new ErrorAlert("ERRORE", "Errore di I/O", "Si e' verificato un errore di I/O. La connessione non puo' essere aperta.");
                                    spinnerPort.setDisable(false);
                                    textFieldAddress.setDisable(false);
                                    buttonConnectionStatus.setDisable(false);
                                });
                                return null;
                            }
                            Platform.runLater(() -> {
                                buttonConnectionStatus.setText("VAI OFFLINE");
                                imageViewConnectionStatus.setImage(IMAGE_ONLINE);
                                buttonConnectionStatus.setDisable(false);
                                buttonDownloadFile.setDisable(false);
                                buttonRefreshFileList.setDisable(false);
                                refreshFileList();
                            });
                            return null;
                        }
                    };
                }
            }.start();
        }
    }
    private void disconnect() {
        if (connection != null) {
            try {
                RawSerializer.sendInt(connection.getOutputStream(), SocketProtocol.getIntByRequest(SocketProtocol.DISCONNECT));
            } catch (IOException e) {
                Logger.log(e);
                Platform.runLater(() -> new ErrorAlert("ERRORE", "Errore di I/O", "Si e' verificato un errore durante la disconnessione. Controlla i log per ulteriori informazioni."));
            }
            try {
                connection.close();
            } catch (IOException e) {
                Logger.log(e);
                Platform.runLater(() -> new ErrorAlert("ERRORE", "Errore di I/O", "Si e' verificato un errore durante la disconnessione. Controlla i log per ulteriori informazioni."));
            }
            connection = null;
        }
        Platform.runLater(() -> {
            tableViewFileList.getItems().clear();
            buttonConnectionStatus.setText("VAI ONLINE");
            imageViewConnectionStatus.setImage(IMAGE_OFFLINE);
            buttonDownloadFile.setDisable(true);
            buttonRefreshFileList.setDisable(true);
            spinnerPort.setDisable(false);
            textFieldAddress.setDisable(false);
            buttonConnectionStatus.setDisable(false);
        });
    }
    @FXML
    private void backToMenu() {
        disconnect();
        Client.getStage().setScene(SceneMainMenu.getScene());
    }
}
