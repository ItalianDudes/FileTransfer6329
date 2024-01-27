package it.italiandudes.bot6329.modules.console;

import it.italiandudes.bot6329.modules.BaseModule;
import it.italiandudes.bot6329.modules.ModuleState;
import it.italiandudes.bot6329.throwables.errors.ModuleError;
import it.italiandudes.bot6329.throwables.exceptions.ModuleException;
import it.italiandudes.idl.common.Logger;
import it.italiandudes.idl.common.StringHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class ModuleConsole extends BaseModule {

    // Attributes
    private Thread consoleReaderThread;

    // Module Management Methods
    @Override
    protected synchronized void loadModule(final boolean isReloading) throws ModuleException, ModuleError {
        Logger.log(MODULE_NAME + " Module Load: Started!");
        moduleLoadPreliminaryCheck(MODULE_NAME, isReloading);
        if (!isReloading) setModuleState(ModuleState.LOADING);

        consoleReaderThread = new ConsoleReaderThread();
        consoleReaderThread.start();

        if (!isReloading) setModuleState(ModuleState.LOADED);
        Logger.log(MODULE_NAME + " Module Load: Successful!");
    }
    @Override
    protected synchronized void unloadModule(final boolean isReloading, final boolean bypassPreliminaryChecks) throws ModuleException, ModuleError {
        Logger.log(MODULE_NAME + " Module Unload: Started!");
        if (!bypassPreliminaryChecks) moduleUnloadPreliminaryCheck(MODULE_NAME, isReloading);
        if (!isReloading) setModuleState(ModuleState.UNLOADING);

        if (consoleReaderThread != null) consoleReaderThread.interrupt();

        if (!isReloading) setModuleState(ModuleState.NOT_LOADED);
        Logger.log(MODULE_NAME + " Module Unload: Successful!");
    }

    // Module Methods
    public void handleUserInput(@Nullable String userInput) throws ModuleException {
        if (getModuleState() != ModuleState.LOADED) throw new ModuleException("Can't parse user input: the console module isn't loaded.");
        if (userInput == null) userInput = "";

        String[] parsedCommand = StringHandler.parseString(userInput);
        String[] commandArgs = null;
        if (parsedCommand.length <= 1) {
            parsedCommand = new String[] {userInput};
        } else {
            commandArgs = Arrays.copyOfRange(parsedCommand, 1, parsedCommand.length);
        }

        ConsoleCommand consoleCommand = ConsoleCommand.getConsoleCommandByName(parsedCommand[0]);
        if (consoleCommand == null) {
            logUnknownMessage();
            return;
        }

        if (commandArgs == null) commandArgs = new String[]{};
        int code = consoleCommand.execute(commandArgs);
        Logger.log("Command execution terminated with code: " + code);
        if (code != 0) {
            Logger.log("[WARNING] Command terminated with non-zero code. This may mean an error has occurred.");
        }
    }

    // Unknown command default message
    private static void logUnknownMessage() {
        Logger.log("Unknown command! Type \"" + ConsoleCommand.HELP.getName() + "\" to show the list of commands");
    }

    // Instance
    private static ModuleConsole instance = null;
    private ModuleConsole() {
        super("Console");
    }
    @NotNull
    public static ModuleConsole getInstance() {
        if (instance == null) instance = new ModuleConsole();
        return instance;
    }
}
