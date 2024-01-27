package it.italiandudes.filetransfer6329.throwables.exceptions.module.generic;

import it.italiandudes.filetransfer6329.throwables.exceptions.ModuleException;

@SuppressWarnings("unused")
public class ModuleNotLoadedException extends ModuleException {
    public ModuleNotLoadedException(String message) {
        super(message);
    }
    public ModuleNotLoadedException(String message, Throwable cause) {
        super(message, cause);
    }
}
