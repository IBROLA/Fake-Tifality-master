package club.tifality.manager.command.impl;

import club.tifality.Tifality;
import club.tifality.manager.command.Command;
import club.tifality.manager.command.CommandExecutionException;
import club.tifality.module.ModuleManager;
import club.tifality.module.impl.render.Hud;
import club.tifality.gui.notification.Notification;
import club.tifality.gui.notification.NotificationType;

public final class ClientNameCommand implements Command {
    @Override
    public String[] getAliases() {
        return new String[]{"clientName", "rename"};
    }

    @Override
    public void execute(String[] arguments) throws CommandExecutionException {
        if (arguments.length >= 2) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < arguments.length; i++)
                sb.append(arguments[i]).append(' ');
            ModuleManager.getInstance(Hud.class).watermarkText.setValue(sb.toString());
            Tifality.getInstance().getNotificationManager().add(new Notification(
                    "Updated clientName",  1500L, NotificationType.SUCCESS));
            return;
        }

        throw new CommandExecutionException(getUsage());
    }

    @Override
    public String getUsage() {
        return "clientName/rename <name>";
    }
}
