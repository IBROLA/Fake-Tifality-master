package club.tifality.manager.command;

public interface Command {

    String[] getAliases();

    void execute(String[] arguments) throws CommandExecutionException;

    String getUsage();

}
