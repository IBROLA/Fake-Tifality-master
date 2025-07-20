package club.tifality.manager.command.impl;

import club.tifality.Tifality;
import club.tifality.manager.command.Command;
import club.tifality.manager.command.CommandExecutionException;
import club.tifality.module.Module;
import club.tifality.utils.Wrapper;

public final class ToggleCommand implements Command {
    @Override
    public String[] getAliases() {
        return new String[]{"toggle", "t"};
    }

    @Override
    public void execute(String[] arguments) throws CommandExecutionException {
        if (arguments.length == 2) {
            String moduleName = arguments[1];

            for (Module module : Tifality.getInstance().getModuleManager().getModules()) {
                if (module.getLabel().replaceAll(" ", "").equalsIgnoreCase(moduleName)) {
                    module.toggle();
                    Wrapper.addChatMessage("'" + module.getLabel() + "' has been " + (module.isEnabled() ? "\247Aenabled\2477." : "\247Cdisabled\2477."));
                    return;
                }
            }
        }

        throw new CommandExecutionException(getUsage());
    }

    @Override
    public String getUsage() {
        return "toggle/t <module>";
    }
}
