package it.italiandudes.filetransfer6329.modules;

import it.italiandudes.filetransfer6329.modules.configuration.ModuleConfiguration;
import it.italiandudes.filetransfer6329.modules.console.ModuleConsole;
import it.italiandudes.filetransfer6329.modules.http.ModuleHTTP;
import it.italiandudes.filetransfer6329.throwables.errors.ModuleError;
import it.italiandudes.filetransfer6329.throwables.exceptions.ModuleException;
import it.italiandudes.idl.common.Logger;

public final class ModuleManager {

    // Initialize the Bot
    public static void init() throws ModuleException, ModuleError {
        ModuleConfiguration.getInstance().loadModule();
        ModuleHTTP.getInstance().loadModule();
        ModuleConsole.getInstance().loadModule();
    }

    // Shutdown the Bot
    public static void shutdown() throws ModuleException, ModuleError {
        Logger.log("Shutdown Initiated!");
        if (ModuleConsole.getInstance().getModuleState() == ModuleState.LOADED)
            ModuleConsole.getInstance().unloadModule();
        if (ModuleHTTP.getInstance().getModuleState() == ModuleState.LOADED)
            ModuleHTTP.getInstance().unloadModule();
        if (ModuleConfiguration.getInstance().getModuleState() == ModuleState.LOADED)
            ModuleConfiguration.getInstance().unloadModule();
        Logger.log("Shutdown Completed!");
        Logger.log("Server Status: OFFLINE");
        Logger.close();
    }

    // Emergency Shutdown the Bot
    public static void emergencyShutdown() {
        Logger.log("Emergency Shutdown Initiated!");
        if (ModuleConsole.getInstance().getModuleState() == ModuleState.LOADED)
            ModuleConsole.getInstance().emergencyUnloadModule();
        if (ModuleHTTP.getInstance().getModuleState() == ModuleState.LOADED)
            ModuleHTTP.getInstance().emergencyUnloadModule();
        if (ModuleConfiguration.getInstance().getModuleState() == ModuleState.LOADED)
            ModuleConfiguration.getInstance().emergencyUnloadModule();
        Logger.log("Emergency Shutdown Completed!");
        Logger.log("Server Status: OFFLINE");
        Logger.close();
    }
}
