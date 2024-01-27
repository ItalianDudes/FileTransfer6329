package it.italiandudes.filetransfer6329.throwables.exceptions.module.database;

import it.italiandudes.filetransfer6329.throwables.exceptions.ModuleException;

@SuppressWarnings("unused")
public class DatabaseModuleException extends ModuleException {
    public DatabaseModuleException(String message) {
        super(message);
    }
    public DatabaseModuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
