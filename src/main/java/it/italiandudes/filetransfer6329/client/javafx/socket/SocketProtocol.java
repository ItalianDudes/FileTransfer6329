package it.italiandudes.filetransfer6329.client.javafx.socket;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum SocketProtocol {
    DISCONNECT(ProtocolUsability.BOTH),
    LIST(ProtocolUsability.RECEIVER_ONLY),
    DOWNLOAD(ProtocolUsability.RECEIVER_ONLY),
    INVALID_MESSAGE(ProtocolUsability.TRANSMITTER_ONLY),
    ERROR(ProtocolUsability.BOTH),
    PROTOCOL_TRANSMITTER_ONLY(ProtocolUsability.TRANSMITTER_ONLY),
    PROTOCOL_RECEIVER_ONLY(ProtocolUsability.RECEIVER_ONLY),
    UNEXPECTED_PROTOCOL(ProtocolUsability.BOTH),
    UNEXPECTED_VALUE(ProtocolUsability.BOTH),
    ID_NOT_AVAILABLE(ProtocolUsability.TRANSMITTER_ONLY),
    FILE_NO_MORE_AVAILABLE(ProtocolUsability.TRANSMITTER_ONLY),
    DOWNLOADING(ProtocolUsability.TRANSMITTER_ONLY),
    DOWNLOAD_COMPLETE(ProtocolUsability.TRANSMITTER_ONLY),
    OK(ProtocolUsability.BOTH),
    DOWNLOAD_CANCELED(ProtocolUsability.RECEIVER_ONLY),
    LAST_BYTE_GROUP(ProtocolUsability.TRANSMITTER_ONLY);

    // Attributes
    private final ProtocolUsability usability;

    // Constructors
    SocketProtocol(@NotNull final ProtocolUsability usability) {
        this.usability = usability;
    }

    // Methods
    @NotNull
    public ProtocolUsability getUsability() {
        return usability;
    }
    @Nullable
    public static SocketProtocol getRequestByInt(final int request) {
        if (request < 0 || request >= SocketProtocol.values().length) return null;
        return SocketProtocol.values()[request];
    }
    public static int getIntByRequest(@NotNull final SocketProtocol protocol) {
        return protocol.ordinal();
    }
}
