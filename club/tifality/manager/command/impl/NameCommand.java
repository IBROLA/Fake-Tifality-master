package club.tifality.manager.command.impl;

import club.tifality.Tifality;
import club.tifality.manager.command.Command;
import club.tifality.manager.command.CommandExecutionException;
import club.tifality.gui.notification.Notification;
import club.tifality.gui.notification.NotificationType;
import club.tifality.utils.ClipboardUtils;
import club.tifality.utils.Wrapper;

public final class NameCommand implements Command {
    @Override
    public String[] getAliases() {
        return new String[]{"name", "copy", "ign"};
    }

    @Override
    public void execute(String[] arguments) throws CommandExecutionException {
        String name = Wrapper.getPlayer().getGameProfile().getName();
        ClipboardUtils.setClipboardContents(name);
        Tifality.getInstance().getNotificationManager().add(
                new Notification("Name Command",
                        String.format("'%s' has been copied to clipboard", name),
                        NotificationType.SUCCESS));
    }


    @Override
    public String getUsage() {
        return "name/copy/ign";
    }
}
