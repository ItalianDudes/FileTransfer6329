package it.italiandudes.filetransfer6329.client.javafx.data;

import it.italiandudes.filetransfer6329.utils.Defs;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;

public final class ServerElement {

    // ID Progressive
    private static int ID_PROGRESSIVE = 0;

    // Attributes
    private final int id;
    @NotNull private final String filename;
    @NotNull private final String fileAbsolutePath;
    private final long fileSizeKB;

    // Constructors
    public ServerElement(@NotNull final File file) throws FileNotFoundException {
        if (!file.exists() || !file.isFile()) throw new FileNotFoundException("File \"" + file.getAbsolutePath() + "\" not found!");
        this.id = ID_PROGRESSIVE;
        ID_PROGRESSIVE++;
        this.filename = file.getName();
        this.fileAbsolutePath = file.getAbsolutePath();
        this.fileSizeKB = file.length() / 1024L;
    }

    // Methods
    public int getId() {
        return id;
    }
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

        ServerElement element = (ServerElement) o;

        if (getId() != element.getId()) return false;
        if (getFileSizeKB() != element.getFileSizeKB()) return false;
        if (!getFilename().equals(element.getFilename())) return false;
        return getFileAbsolutePath().equals(element.getFileAbsolutePath());
    }
    @Override
    public int hashCode() {
        int result = getId();
        result = 31 * result + getFilename().hashCode();
        result = 31 * result + getFileAbsolutePath().hashCode();
        result = 31 * result + (int) (getFileSizeKB() ^ (getFileSizeKB() >>> 32));
        return result;
    }
    @Override
    public String toString() {
        return filename;
    }
    public JSONObject toJSONObject() {
        JSONObject object = new JSONObject();
        object.put(Defs.ElementJSONKeys.ID, id);
        object.put(Defs.ElementJSONKeys.FILENAME, filename);
        object.put(Defs.ElementJSONKeys.FILESIZE_KB, fileSizeKB);
        return object;
    }
}
