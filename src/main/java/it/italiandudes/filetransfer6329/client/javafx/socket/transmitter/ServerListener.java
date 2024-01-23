package it.italiandudes.filetransfer6329.client.javafx.socket.transmitter;

import it.italiandudes.filetransfer6329.client.javafx.controller.ControllerSceneTransmitter;
import it.italiandudes.filetransfer6329.utils.Defs;
import it.italiandudes.idl.common.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public final class ServerListener extends Thread {

    // Attributes
    @NotNull private final ServerSocket serverSocket;

    // Constructors
    public ServerListener(@NotNull final ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.setDaemon(true);
    }

    // Runnable
    @Override
    public void run() {
        try {
            //noinspection InfiniteLoopStatement
            while (true) {
                Socket incomingConnection = serverSocket.accept();
                if (ControllerSceneTransmitter.activeConnections.containsKey(incomingConnection)) {
                    Logger.log("Socket already connected, closing connection...");
                    try {
                        incomingConnection.close();
                    } catch (Exception ignored) {}
                } else {
                    incomingConnection.setSendBufferSize(Defs.BYTE_ARRAY_MAX_SIZE * 2);
                    incomingConnection.setReceiveBufferSize(Defs.BYTE_ARRAY_MAX_SIZE * 2);
                    ClientHandler handler = new ClientHandler(incomingConnection);
                    ControllerSceneTransmitter.activeConnections.put(incomingConnection, handler);
                    handler.start();
                }
            }
        } catch (IOException e) {
            if (!serverSocket.isClosed()) {
                Logger.log(e);
                try {
                    serverSocket.close();
                } catch (Exception ignored) {}
            }
        }
    }
}
