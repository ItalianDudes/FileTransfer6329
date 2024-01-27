package it.italiandudes.filetransfer6329.throwables.exceptions.module.generic;

import it.italiandudes.filetransfer6329.throwables.exceptions.ModuleException;

@SuppressWarnings("unused")
public class ModuleAlreadyLoadedException extends ModuleException {
    public ModuleAlreadyLoadedException(String message) {
        super(message);
    }
    public ModuleAlreadyLoadedException(String message, Throwable cause) {
        super(message, cause);
    }
}
