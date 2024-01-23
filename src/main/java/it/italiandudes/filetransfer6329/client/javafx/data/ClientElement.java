package it.italiandudes.filetransfer6329.client.javafx.data;

import it.italiandudes.filetransfer6329.utils.Defs;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public final class ClientElement {

    // Attributes
    private final int id;
    @NotNull
    private final String filename;
    private final long fileSizeKB;

    // Constructors
    public ClientElement(@NotNull final JSONObject fileJSON) {
        id = fileJSON.getInt(Defs.ElementJSONKeys.ID);
        filename = fileJSON.getString(Defs.ElementJSONKeys.FILENAME);
        fileSizeKB = fileJSON.getLong(Defs.ElementJSONKeys.FILESIZE_KB);
    }

    // Methods
    public int getId() {
        return id;
    }
    @NotNull
    public String getFilename() {
        return filename;
    }
    public long getFileSizeKB() {
        return fileSizeKB;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientElement)) return false;

        ClientElement that = (ClientElement) o;

        if (getId() != that.getId()) return false;
        if (getFileSizeKB() != that.getFileSizeKB()) return false;
        return getFilename().equals(that.getFilename());
    }
    @Override
    public int hashCode() {
        int result = getId();
        result = 31 * result + getFilename().hashCode();
        result = 31 * result + (int) (getFileSizeKB() ^ (getFileSizeKB() >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return filename;
    }
}
