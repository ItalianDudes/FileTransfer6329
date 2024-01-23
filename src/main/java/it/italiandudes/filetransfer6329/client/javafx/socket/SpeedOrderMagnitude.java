package it.italiandudes.filetransfer6329.client.javafx.socket;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public enum SpeedOrderMagnitude {
    BYTE_PER_SECOND(1),
    KB_PER_SECOND(BYTE_PER_SECOND.bytes * 1024),
    MB_PER_SECOND(KB_PER_SECOND.bytes * 1024),
    GB_PER_SECOND(MB_PER_SECOND.bytes * 1024);

    // Attributes
    private final int bytes;

    // Constructors
    SpeedOrderMagnitude(final int bytes) {
        this.bytes = bytes;
    }

    // Methods
    public static List<SpeedOrderMagnitude> getList() {
        return Arrays.asList(SpeedOrderMagnitude.values());
    }
    public static SpeedOrderMagnitude convert(@NotNull final SpeedOrderMagnitude from, @NotNull final SpeedOrderMagnitude to) {
        int order = Math.max(BYTE_PER_SECOND.ordinal(), from.ordinal() - to.ordinal());
        if (order >= SpeedOrderMagnitude.values().length) order = GB_PER_SECOND.ordinal();
        return SpeedOrderMagnitude.values()[order];
    }
    public int getBytes() {
        return bytes;
    }
    @Override
    public String toString() {
        switch (this) {
            case BYTE_PER_SECOND:
                return "B/s";
            case KB_PER_SECOND:
                return "KB/s";
            case MB_PER_SECOND:
                return "MB/s";
            case GB_PER_SECOND:
                return "GB/s";
            default:
                return "NA";
        }
    }
}
