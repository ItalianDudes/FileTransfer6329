package it.italiandudes.filetransfer6329.modules.console.commands;

import it.italiandudes.filetransfer6329.modules.ModuleManager;
import it.italiandudes.filetransfer6329.throwables.exceptions.ModuleException;
import it.italiandudes.idl.common.Logger;
import org.jetbrains.annotations.NotNull;

public final class StopConsoleCommand extends BaseConsoleCommand {

    // Constructors
    public StopConsoleCommand() {
        super(
                "stop",
                "stop",
                "Initiate the bot shutdown procedure."
        );
    }

    // Methods
    @Override
    public int execute(@NotNull String[] arguments) {
        try {
            ModuleManager.shutdown();
        } catch (ModuleException e) {
            Logger.log("An error has occurred during bot shutdown, emergency shutdown initiated");
            ModuleManager.emergencyShutdown();
        }
        return 0;
    }
}
