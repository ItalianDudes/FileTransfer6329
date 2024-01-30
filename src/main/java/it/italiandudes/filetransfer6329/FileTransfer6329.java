package it.italiandudes.filetransfer6329;

import it.italiandudes.filetransfer6329.modules.ModuleManager;
import it.italiandudes.filetransfer6329.modules.configuration.ConfigurationMap;
import it.italiandudes.filetransfer6329.modules.configuration.ModuleConfiguration;
import it.italiandudes.filetransfer6329.modules.console.ConsoleCommand;
import it.italiandudes.filetransfer6329.modules.http.ModuleHTTP;
import it.italiandudes.filetransfer6329.throwables.errors.ModuleError;
import it.italiandudes.filetransfer6329.throwables.exceptions.ModuleException;
import it.italiandudes.idl.common.InfoFlags;
import it.italiandudes.idl.common.Logger;
import it.italiandudes.idl.common.StringHandler;

import java.io.IOException;

public final class FileTransfer6329 {

    // Main Method
    public static void main(String[] args) {

        // Initializing the logger
        try {
            Logger.init();
        } catch (IOException e) {
            Logger.log("An error has occurred during Logger initialization, exit...");
            return;
        }

        // Configure the shutdown hooks
        Runtime.getRuntime().addShutdownHook(new Thread(Logger::close));
        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> {
            Logger.log(StringHandler.getStackTrace(e));
            ModuleManager.emergencyShutdown();
        });

        // Server Initialization
        try {
            ModuleManager.init();
            Logger.log("Server Status: ONLINE");
            Logger.log("Server Port: " + ModuleConfiguration.getInstance().getConfigValue(ConfigurationMap.Keys.SERVER_PORT));
            Logger.log("Server Root Directory: " + ModuleHTTP.getInstance().getRootDirectory().getAbsolutePath());
            Logger.log("Type \"" + ConsoleCommand.HELP.getName() + "\" to see the list of all commands.");
        } catch (ModuleException | ModuleError e) {
            Logger.log(e, new InfoFlags(true, true));
            ModuleManager.emergencyShutdown();
            Logger.close();
        }
    }
}
