package it.italiandudes.filetransfer6329.client.javafx.data;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;

public final class ServerElement {

    // Attributes
    @NotNull private final String filename;
    @NotNull private final String fileAbsolutePath;
    private final long fileSizeKB;

    // Constructors
    public ServerElement(@NotNull final File file) throws FileNotFoundException {
        if (!file.exists() || !file.isFile()) throw new FileNotFoundException("File \"" + file.getAbsolutePath() + "\" not found!");
        this.filename = file.getName();
        this.fileAbsolutePath = file.getAbsolutePath();
        this.fileSizeKB = file.length() / 1024L;
    }

    // Methods
    @NotNull
    public String getFilename() {
        return filename;
    }
    @NotNull
    public String getFileAbsolutePath() {
        return fileAbsolutePath;
    }
    public long getFileSizeKB() {
        return fileSizeKB;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerElement)) return false;

        ServerElement that = (ServerElement) o;

        if (getFileSizeKB() != that.getFileSizeKB()) return false;
        if (!getFilename().equals(that.getFilename())) return false;
        return getFileAbsolutePath().equals(that.getFileAbsolutePath());
    }
    @Override
    public int hashCode() {
        int result = getFilename().hashCode();
        result = 31 * result + getFileAbsolutePath().hashCode();
        result = 31 * result + (int) (getFileSizeKB() ^ (getFileSizeKB() >>> 32));
        return result;
    }
    @Override
    public String toString() {
        return getFileAbsolutePath();
    }
}
