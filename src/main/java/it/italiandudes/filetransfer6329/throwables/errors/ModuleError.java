package it.italiandudes.filetransfer6329.throwables.errors;

@SuppressWarnings("unused")
public class ModuleError extends Error {
    public ModuleError(String message) {
        super(message);
    }
    public ModuleError(String message, Throwable cause) {
        super(message, cause);
    }
}
