package it.italiandudes.filetransfer6329.client.javafx.controller;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public final class ControllerSceneDownloadProgressBar {

    // Attributes
    private boolean initialized = false;
    private final long totalBytes = ControllerSceneReceiver.getTotalBytes();
    private boolean downloadEnd = false;
    private static final long SLEEP_MILLIS = 5000L;

    // Graphic Elements
    @FXML private AnchorPane mainPane;
    @FXML private ProgressBar progressBar;
    @FXML private Label labelCurrentValue;
    @FXML private Label labelTotalValue;
    @FXML private Button buttonCancelDownload;

    // Initialize
    @FXML
    private void initialize() {
        ((Stage)(mainPane.getScene().getWindow())).setResizable(false);
        initializeDownloadProgress(totalBytes);
        ControllerSceneReceiver.setStageShowed();
        new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() {
                        try {
                            while (!downloadEnd && ControllerSceneReceiver.getCurrentBytes() < totalBytes) {
                                //noinspection BusyWait
                                Thread.sleep(SLEEP_MILLIS);
                                updateDownloadProgress(ControllerSceneReceiver.getCurrentBytes());
                            }
                        } catch (InterruptedException ignored) {}
                        updateDownloadProgress(ControllerSceneReceiver.getCurrentBytes());
                        return null;
                    }
                };
            }
        }.start();
    }

    // Methods
    public void updateDownloadProgress(long currentBytes) {
        if (downloadEnd) return;
        String formattedCurrent = formatByteNumber(currentBytes);
        Platform.runLater(() -> {
            labelCurrentValue.setText(formattedCurrent);
            progressBar.setProgress((double) currentBytes / (double) totalBytes);
            if (currentBytes >= totalBytes) {
                downloadEnd = true;
                ((Stage)(mainPane.getScene().getWindow())).close();
            }
        });
    }
    public void initializeDownloadProgress(long totalValueBytes) {
        if (initialized) return;
        String formattedTotal = formatByteNumber(totalValueBytes);
        Platform.runLater(() -> {
            labelTotalValue.setText(formattedTotal);
            labelCurrentValue.setText("0B");
            progressBar.setProgress(0);
        });
        initialized = true;
    }
    private String formatByteNumber(long bytes) {
        String[] units = new String[] {"B", "KB", "MB", "GB", "TB"};
        int unit = 0;
        double size = bytes;
        while (size >= 1024 && unit < units.length - 1) {
            size /= 1024;
            unit++;
        }
        return String.format("%.1f %s", size, units[unit]);
    }

    // EDT
    @FXML
    private void cancelDownload() {
        buttonCancelDownload.setDisable(true);
        downloadEnd = true;
        ControllerSceneReceiver.setDownloadCanceled();
        ((Stage)(mainPane.getScene().getWindow())).close();
    }
}
