package club.tifality.manager.command.impl;

import club.tifality.Tifality;
import club.tifality.manager.command.Command;
import club.tifality.manager.command.CommandExecutionException;
import club.tifality.module.Module;
import club.tifality.gui.notification.Notification;
import club.tifality.gui.notification.NotificationType;

import java.util.Optional;

public final class VisibleCommand implements Command {
    @Override
    public String[] getAliases() {
        return new String[]{"visible", "v"};
    }

    @Override
    public void execute(String[] arguments) throws CommandExecutionException {
        if (arguments.length == 2) {
            String moduleName = arguments[1];

            Optional<Module> module = Optional.ofNullable(Tifality.getInstance().getModuleManager().getModule(moduleName));

            if (module.isPresent()) {
                Module m = module.get();
                m.setHidden(!m.isHidden());
                Tifality.getInstance().getNotificationManager().add(new Notification(
                        "Set '" + m.getLabel() + "' to " + (m.isHidden() ? "hidden" : "visible"),
                        NotificationType.SUCCESS));
                return;
            }
        }
        throw new CommandExecutionException(getUsage());
    }

    @Override
    public String getUsage() {
        return "visible/v <module>";
    }
}
