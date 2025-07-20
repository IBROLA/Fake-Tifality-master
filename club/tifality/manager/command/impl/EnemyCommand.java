package club.tifality.manager.command.impl;

import club.tifality.Tifality;
import club.tifality.manager.command.Command;
import club.tifality.manager.command.CommandExecutionException;
import club.tifality.gui.notification.Notification;
import club.tifality.gui.notification.NotificationType;

public final class EnemyCommand implements Command {
    @Override
    public String[] getAliases() {
        return new String[]{"enemy", "e"};
    }

    @Override
    public void execute(String[] arguments) throws CommandExecutionException {
        enemy:
        {
            if (arguments.length <= 1)
                break enemy;
            final String usernameOrAction = arguments[1].toUpperCase();
            String username = usernameOrAction;
            switch (usernameOrAction) {
                case "ADD":
                    if (arguments.length <= 2)
                        break enemy;
                    username = arguments[2];
                default:
                    final String alias;
                    if (arguments.length >= 4) {
                        alias = arguments[3];
                    } else {
                        alias = null;
                    }
                    final String enemyAdded = Tifality.getInstance().getPlayerManager()
                            .add(username, alias, false);
                    if (enemyAdded != null) {
                        Tifality.getInstance().getNotificationManager().add(new Notification(
                                "Enemy Added",
                                String.format("%s is now considered an enemy.", enemyAdded),
                                1500L,
                                NotificationType.SUCCESS));
                    } else {
                        Tifality.getInstance().getNotificationManager().add(new Notification(
                                String.format("%s is not a player.", username), 1000L, NotificationType.ERROR));
                    }
                    return;
                case "DEL":
                case "REMOVE":
                    if (arguments.length != 3)
                        break enemy;
                    username = arguments[2];

                    final String enemyRemoved = Tifality.getInstance().getPlayerManager()
                            .remove(username);
                    if (enemyRemoved != null) {
                        Tifality.getInstance().getNotificationManager().add(new Notification(
                                "Enemy Removed",
                                String.format("%s is no longer considered an enemy.", enemyRemoved),
                                1500L,
                                NotificationType.SUCCESS));
                    } else {
                        Tifality.getInstance().getNotificationManager().add(new Notification(
                                String.format("%s is not considered an enemy.", username), 1000L, NotificationType.ERROR));
                    }

            }
        }

        throw new CommandExecutionException(getUsage());
    }

    @Override
    public String getUsage() {
        return "enemy/e <(optional)add/remove/del> <username> <(optional)alias>";
    }
}
