package it.italiandudes.filetransfer6329.client.javafx.socket.transmitter;

import org.jetbrains.annotations.NotNull;

import java.net.Socket;

public class ClientHandler extends Thread {

    // Attributes
    @NotNull private final Socket connection;

    // Constructors
    public ClientHandler(@NotNull final Socket connection) {
        this.connection = connection;
        this.setDaemon(true);
    }

    // Runnable
    @Override
    public void run() {
        // TODO: implement ClientHandler.run()
    }
}
