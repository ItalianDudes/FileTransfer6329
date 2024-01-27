package it.italiandudes.filetransfer6329.throwables.exceptions.module.configuration;

import it.italiandudes.filetransfer6329.throwables.exceptions.ModuleException;

@SuppressWarnings("unused")
public class ConfigurationModuleException extends ModuleException {
    public ConfigurationModuleException(String message) {
        super(message);
    }
    public ConfigurationModuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
