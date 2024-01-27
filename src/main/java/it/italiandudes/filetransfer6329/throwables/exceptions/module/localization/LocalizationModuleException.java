package it.italiandudes.filetransfer6329.throwables.exceptions.module.localization;

import it.italiandudes.filetransfer6329.throwables.exceptions.ModuleException;

@SuppressWarnings("unused")
public class LocalizationModuleException extends ModuleException {
    public LocalizationModuleException(String message) {
        super(message);
    }
    public LocalizationModuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
