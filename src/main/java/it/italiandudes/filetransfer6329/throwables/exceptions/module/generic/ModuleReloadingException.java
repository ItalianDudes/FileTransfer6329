package it.italiandudes.filetransfer6329.throwables.exceptions.module.generic;

import it.italiandudes.filetransfer6329.throwables.exceptions.ModuleException;

@SuppressWarnings("unused")
public final class ModuleReloadingException extends ModuleException {
    public ModuleReloadingException(String message) {
        super(message);
    }
    public ModuleReloadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
