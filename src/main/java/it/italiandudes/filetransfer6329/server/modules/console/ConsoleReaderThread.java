package it.italiandudes.bot6329.modules.console;

import it.italiandudes.bot6329.throwables.exceptions.ModuleException;

import java.util.Scanner;

public class ConsoleReaderThread extends Thread {

    // Attributes
    private final Scanner stdin;

    // Constructors
    public ConsoleReaderThread() {
        this.stdin = new Scanner(System.in);
        this.setDaemon(true);
    }

    // Thread Method
    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                String userInput = stdin.nextLine();
                ModuleConsole.getInstance().handleUserInput(userInput);
            }
        } catch (ModuleException ignored) {} // This is fine, is another way to stop this thread if the module goes in error or gets unloaded.
    }
}
