package it.italiandudes.filetransfer6329.server.modules;

import it.italiandudes.bot6329.modules.configuration.ModuleConfiguration;
import it.italiandudes.bot6329.modules.console.ModuleConsole;
import it.italiandudes.bot6329.modules.database.ModuleDatabase;
import it.italiandudes.bot6329.modules.localization.ModuleLocalization;
import it.italiandudes.filetransfer6329.throwables.errors.ModuleError;
import it.italiandudes.filetransfer6329.throwables.exceptions.ModuleException;
import it.italiandudes.idl.common.Logger;

public final class ModuleManager {

    // Initialize the Bot
    public static void initBot() throws ModuleException, ModuleError {
        ModuleConfiguration.getInstance().loadModule();
        ModuleLocalization.getInstance().loadModule();
        ModuleDatabase.getInstance().loadModule();
        ModuleConsole.getInstance().loadModule();
    }

    // Shutdown the Bot
    public static void shutdownBot() throws ModuleException, ModuleError {
        Logger.log("Shutdown Initiated!");
        if (ModuleConsole.getInstance().getModuleState() == ModuleState.LOADED)
            ModuleConsole.getInstance().unloadModule();
        if (ModuleDatabase.getInstance().getModuleState() == ModuleState.LOADED)
            ModuleDatabase.getInstance().unloadModule();
        if (ModuleLocalization.getInstance().getModuleState() == ModuleState.LOADED)
            ModuleLocalization.getInstance().unloadModule();
        if (ModuleConfiguration.getInstance().getModuleState() == ModuleState.LOADED)
            ModuleConfiguration.getInstance().unloadModule();
        Logger.log("Shutdown Completed!");
        Logger.log("Bot Status: OFFLINE");
        Logger.close();
    }

    // Emergency Shutdown the Bot
    public static void emergencyShutdownBot() {
        Logger.log("Emergency Shutdown Initiated!");
        if (ModuleConsole.getInstance().getModuleState() == ModuleState.LOADED)
            ModuleConsole.getInstance().emergencyUnloadModule();
        if (ModuleDatabase.getInstance().getModuleState() == ModuleState.LOADED)
            ModuleDatabase.getInstance().emergencyUnloadModule();
        if (ModuleLocalization.getInstance().getModuleState() == ModuleState.LOADED)
            ModuleLocalization.getInstance().emergencyUnloadModule();
        if (ModuleConfiguration.getInstance().getModuleState() == ModuleState.LOADED)
            ModuleConfiguration.getInstance().emergencyUnloadModule();
        Logger.log("Emergency Shutdown Completed!");
        Logger.log("Bot Status: OFFLINE");
        Logger.close();
    }
}
