package it.italiandudes.filetransfer6329.client.javafx.socket.transmitter;

import it.italiandudes.idl.common.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Set;

public final class ServerListener extends Thread {

    // Attributes
    @NotNull private final ServerSocket serverSocket;
    @NotNull private final HashMap<Socket, ClientHandler> activeConnections;

    // Constructors
    public ServerListener(@NotNull final ServerSocket serverSocket, @NotNull final HashMap<Socket, ClientHandler> activeConnections) {
        this.serverSocket = serverSocket;
        this.activeConnections = activeConnections;
        this.setDaemon(true);
    }

    // Runnable
    @Override
    public void run() {
        try {
            //noinspection InfiniteLoopStatement
            while (true) {
                Socket incomingConnection = serverSocket.accept();
                if (activeConnections.containsKey(incomingConnection)) {
                    try {
                        incomingConnection.close();
                    } catch (Exception ignored) {
                    }
                } else {
                    ClientHandler handler = new ClientHandler(incomingConnection);
                    activeConnections.put(incomingConnection, handler);
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
        }
    }
}
