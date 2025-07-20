package club.tifality.gui.notification.dev;

import club.tifality.Tifality;
import club.tifality.module.impl.render.Debug;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DevNotifications {
    private static DevNotifications instance = new DevNotifications();
    private final List<DevNotification> notifications = new CopyOnWriteArrayList<>();
    private final DevNotificationRenderer renderer = new DevNotificationRenderer();

    private DevNotifications() {
        instance = this;
    }

    public static DevNotifications getManager() {
        return instance;
    }

    public void post(String text) {
        System.out.println(text);
        if (Tifality.getInstance().getModuleManager().getModule(Debug.class).isEnabled()) {
            this.notifications.add(new DevNotification(text));
            Tifality.getSourceConsoleGUI().sourceConsole.addStringList(text);
        }
    }

    public void updateAndRender() {
        if (!this.notifications.isEmpty()) {
            this.renderer.draw(this.notifications);
        }
    }
}

