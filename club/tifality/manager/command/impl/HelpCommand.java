package club.tifality.manager.command.impl;

import club.tifality.Tifality;
import club.tifality.manager.command.Command;
import club.tifality.manager.command.CommandExecutionException;
import club.tifality.utils.Wrapper;

import java.util.Arrays;

public final class HelpCommand implements Command {
    @Override
    public String[] getAliases() {
        return new String[]{"help", "h"};
    }

    @Override
    public void execute(String[] arguments) throws CommandExecutionException {
        Wrapper.addChatMessage("Available Commands:");
        for (Command command : Tifality.getInstance().getCommandHandler().getElements()) {
            if (Tifality.getInstance().getModuleManager().getModules().stream().noneMatch(module ->
                    Arrays.stream(command.getAliases()).anyMatch(alias -> alias.equalsIgnoreCase(module.getLabel())))) {
                Wrapper.addChatMessage(Arrays.toString(command.getAliases()) + ": " + command.getUsage());
            }
        }
    }

    @Override
    public String getUsage() {
        return "help/h";
    }
}
