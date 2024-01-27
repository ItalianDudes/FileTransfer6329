package it.italiandudes.filetransfer6329.throwables.exceptions.module.generic;

import it.italiandudes.filetransfer6329.throwables.exceptions.ModuleException;

@SuppressWarnings("unused")
public class ModuleLoadingException extends ModuleException {
    public ModuleLoadingException(String message) {
        super(message);
    }
    public ModuleLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
