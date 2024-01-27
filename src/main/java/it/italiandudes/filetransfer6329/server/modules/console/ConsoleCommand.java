package it.italiandudes.bot6329.modules.console;

import it.italiandudes.bot6329.modules.console.commands.BaseConsoleCommand;
import it.italiandudes.bot6329.modules.console.commands.HelpConsoleCommand;
import it.italiandudes.bot6329.modules.console.commands.StopConsoleCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum ConsoleCommand {
    HELP(new HelpConsoleCommand()),
    STOP(new StopConsoleCommand()),
    ;

    // Attributes
    @NotNull private final BaseConsoleCommand command;

    // Constructors
    ConsoleCommand(@NotNull final BaseConsoleCommand command) {
        this.command = command;
    }

    // Methods
    @Nullable
    public static ConsoleCommand getConsoleCommandByName(@NotNull final String name) {
        for (ConsoleCommand command : ConsoleCommand.values()) {
            if (command.getName().equals(name.toLowerCase())) return command;
        }
        return null;
    }
    public final int execute(@NotNull final String[] args) {
        return command.execute(args);
    }
    public final String getDescription() {
        return command.getDescription();
    }
    public final String getName() {
        return command.getName();
    }
    public final String getSynopsis() {
        return command.getSynopsis();
    }
    @Override
    public String toString() {
        return command.toString();
    }
}
