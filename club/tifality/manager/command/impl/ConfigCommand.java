package club.tifality.manager.command.impl;

import club.tifality.Tifality;
import club.tifality.manager.command.Command;
import club.tifality.manager.command.CommandExecutionException;
import club.tifality.manager.config.Config;
import club.tifality.gui.notification.Notification;
import club.tifality.gui.notification.NotificationType;
import club.tifality.utils.Wrapper;

public final class ConfigCommand implements Command {
    @Override
    public String[] getAliases() {
        return new String[]{"config", "c", "preset"};
    }

    @Override
    public void execute(String[] arguments) throws CommandExecutionException {
        if (arguments.length >= 2) {
            String upperCaseFunction = arguments[1].toUpperCase();

            if (arguments.length == 3) {
                switch (upperCaseFunction) {
                    case "LOAD":
                        if (Tifality.getInstance().getConfigManager().loadConfig(arguments[2]))
                            success("loaded", arguments[2]);
                        else
                            fail("load", arguments[2]);
                        return;
                    case "SAVE":
                        if (Tifality.getInstance().getConfigManager().saveConfig(arguments[2]))
                            success("saved", arguments[2]);
                        else
                            fail("save", arguments[2]);
                        return;
                    case "DELETE":
                        if (Tifality.getInstance().getConfigManager().deleteConfig(arguments[2]))
                            success("deleted", arguments[2]);
                        else
                            fail("delete", arguments[2]);
                        return;
                }
            } else if (arguments.length == 2 && upperCaseFunction.equalsIgnoreCase("LIST")) {
                Wrapper.addChatMessage("Available Configs:");
                for (Config config : Tifality.getInstance().getConfigManager().getElements())
                    Wrapper.addChatMessage(config.getName());
                return;
            }
        }

        throw new CommandExecutionException(getUsage());
    }

    private void success(String type, String configName) {
        Tifality.getInstance().getNotificationManager().add(new Notification(
                String.format("Successfully %s config: '%s'", type, configName), NotificationType.SUCCESS));
    }

    private void fail(String type, String configName) {
        Tifality.getInstance().getNotificationManager().add(new Notification(
                String.format("Failed to %s config: '%s'", type, configName), NotificationType.ERROR));
    }

    @Override
    public String getUsage() {
        return "config/c/preset <load/save/delete/list> <(optional)config>";
    }
}
