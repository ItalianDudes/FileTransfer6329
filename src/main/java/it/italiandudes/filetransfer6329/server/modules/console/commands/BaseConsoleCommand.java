package it.italiandudes.bot6329.modules.console.commands;

import org.jetbrains.annotations.NotNull;

public abstract class BaseConsoleCommand {

    // Attributes
    @NotNull protected final String name;
    @NotNull protected final String synopsis;
    @NotNull protected final String description;

    // Constructors
    protected BaseConsoleCommand(@NotNull final String name, @NotNull final String synopsis, @NotNull final String description) {
        this.name = name;
        this.synopsis = synopsis;
        this.description = description;
    }

    // Methods
    @NotNull
    public final String getName() {
        return name;
    }
    @NotNull
    public final String getSynopsis() {
        return synopsis;
    }
    @NotNull
    public final String getDescription() {
        return description;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseConsoleCommand that)) return false;

        if (!getName().equals(that.getName())) return false;
        if (!getSynopsis().equals(that.getSynopsis())) return false;
        return getDescription().equals(that.getDescription());
    }
    @Override
    public int hashCode() {
        int result = getName().hashCode();
        result = 31 * result + getSynopsis().hashCode();
        result = 31 * result + getDescription().hashCode();
        return result;
    }
    @Override
    public String toString() {
        return name + ":\n" +
                "SYNOPSIS:" + '\n' +
                synopsis + '\n' +
                '\n' +
                "DESCRIPTION:" + '\n' +
                description + '\n';
    }
    /**
     * Execute the command with the provided arguments.
     *
     * @param arguments the list of arguments
     * @return 0 if the execution was successful
     */
    public abstract int execute(@NotNull final String[] arguments);
}
