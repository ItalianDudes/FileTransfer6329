package it.italiandudes.bot6329.modules.console.commands;

import it.italiandudes.bot6329.modules.console.ConsoleCommand;
import it.italiandudes.idl.common.Logger;
import org.jetbrains.annotations.NotNull;

public class HelpConsoleCommand extends BaseConsoleCommand {

    public HelpConsoleCommand() {
        super(
                "help",
                "help [command]",
                "Show the synopsis of all commands or the documentation of the specified command."
        );
    }

    // Command Implementation
    @Override
    public int execute(@NotNull String[] arguments) {
        if (arguments.length == 0) {
            for (ConsoleCommand consoleCommand : ConsoleCommand.values()) {
                Logger.log(consoleCommand.getSynopsis());
            }
        } else {
            ConsoleCommand command = ConsoleCommand.getConsoleCommandByName(arguments[0]);
            if (command == null) {
                Logger.log("Command \"" + arguments[0] + "\" not found. Type \"" + ConsoleCommand.HELP.getName() + "\" to see all commands.");
            } else {
                Logger.log(command.toString());
            }
        }
        return 0;
    }
}
