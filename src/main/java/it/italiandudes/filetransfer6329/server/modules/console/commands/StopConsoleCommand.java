package it.italiandudes.bot6329.modules.console.commands;

import it.italiandudes.bot6329.modules.ModuleManager;
import it.italiandudes.bot6329.throwables.exceptions.ModuleException;
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
            ModuleManager.shutdownBot();
        } catch (ModuleException e) {
            Logger.log("An error has occurred during bot shutdown, emergency shutdown initiated");
            ModuleManager.emergencyShutdownBot();
        }
        return 0;
    }
}
