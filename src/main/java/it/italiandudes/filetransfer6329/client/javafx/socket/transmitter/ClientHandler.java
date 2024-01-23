package it.italiandudes.filetransfer6329.client.javafx.socket.transmitter;

import it.italiandudes.filetransfer6329.client.javafx.controller.ControllerSceneTransmitter;
import it.italiandudes.filetransfer6329.client.javafx.data.ServerElement;
import it.italiandudes.filetransfer6329.client.javafx.socket.ProtocolUsability;
import it.italiandudes.filetransfer6329.client.javafx.socket.SocketProtocol;
import it.italiandudes.filetransfer6329.utils.Defs;
import it.italiandudes.idl.common.Logger;
import it.italiandudes.idl.common.RawSerializer;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;

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
        try {
            while (true) {
                int request = RawSerializer.receiveInt(connection.getInputStream());
                SocketProtocol protocol = SocketProtocol.getRequestByInt(request);
                if (protocol == null) { // Invalid Request
                    RawSerializer.sendInt(connection.getOutputStream(), SocketProtocol.getIntByRequest(SocketProtocol.INVALID_MESSAGE));
                } else if (protocol.getUsability() == ProtocolUsability.TRANSMITTER_ONLY) { // The request is transmitter-only
                    RawSerializer.sendInt(connection.getOutputStream(), SocketProtocol.getIntByRequest(SocketProtocol.PROTOCOL_TRANSMITTER_ONLY));
                } else {
                    HashSet<ServerElement> elementSet;
                    switch (protocol) {
                        case LIST:
                            elementSet = ControllerSceneTransmitter.getElementList();
                            JSONArray elementArray = new JSONArray();
                            for (ServerElement element : elementSet) {
                                elementArray.put(element.toJSONObject());
                            }
                            String base64elements = Base64.getEncoder().encodeToString(elementArray.toString().getBytes(StandardCharsets.UTF_8));
                            RawSerializer.sendString(connection.getOutputStream(), base64elements);
                            break;

                        case DOWNLOAD:
                            int id = RawSerializer.receiveInt(connection.getInputStream());
                            if (id < 0) {
                                RawSerializer.sendInt(connection.getOutputStream(), SocketProtocol.getIntByRequest(SocketProtocol.UNEXPECTED_VALUE));
                                break;
                            }
                            elementSet = ControllerSceneTransmitter.getElementList();
                            ServerElement element = null;
                            for (ServerElement listElement : elementSet) {
                                if (listElement.getId() == id) element = listElement;
                            }
                            if (element == null) {
                                RawSerializer.sendInt(connection.getOutputStream(), SocketProtocol.getIntByRequest(SocketProtocol.ID_NOT_AVAILABLE));
                            } else {
                                File filePointer = new File(element.getFileAbsolutePath());
                                try (FileInputStream inputStream = new FileInputStream(filePointer)) {
                                    long filesize = filePointer.length();
                                    RawSerializer.sendInt(connection.getOutputStream(), SocketProtocol.getIntByRequest(SocketProtocol.DOWNLOADING));
                                    RawSerializer.sendLong(connection.getOutputStream(), filesize);
                                    byte[] buffer = new byte[Defs.BYTE_ARRAY_MAX_SIZE];
                                    long bytesSent = 0;
                                    int bytesRead;
                                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                                        connection.getOutputStream().write(buffer, 0, bytesRead);
                                        connection.getOutputStream().flush();
                                        bytesSent += bytesRead;
                                        Logger.log(bytesSent + " / " + filesize);
                                        if (SocketProtocol.getRequestByInt(RawSerializer.receiveInt(connection.getInputStream())) != SocketProtocol.OK) {
                                            throw new IOException("A client error has occurred, this connection is terminated");
                                        }
                                        if (bytesSent == filesize) break;
                                    }
                                    RawSerializer.sendInt(connection.getOutputStream(), SocketProtocol.getIntByRequest(SocketProtocol.DOWNLOAD_COMPLETE));
                                } catch (FileNotFoundException e) {
                                    Logger.log(e);
                                    RawSerializer.sendInt(connection.getOutputStream(), SocketProtocol.getIntByRequest(SocketProtocol.FILE_NO_MORE_AVAILABLE));
                                    ControllerSceneTransmitter.removeUnavailableElementFromList(element);
                                }
                            }
                            break;

                        case DISCONNECT:
                            ControllerSceneTransmitter.activeConnections.remove(connection);
                            connection.close();
                            return;

                        default:
                            RawSerializer.sendInt(connection.getOutputStream(), SocketProtocol.getIntByRequest(SocketProtocol.UNEXPECTED_PROTOCOL));
                            break;
                    }
                }
            }
        } catch (IOException e) {
            try {
                RawSerializer.sendInt(connection.getOutputStream(), SocketProtocol.getIntByRequest(SocketProtocol.ERROR));
            } catch (Exception ignored) {}
            Logger.log(e);
            ControllerSceneTransmitter.activeConnections.remove(connection);
            try {
                connection.close();
            } catch (Exception ignored) {}
        }
    }
}
