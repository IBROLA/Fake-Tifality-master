package club.tifality.gui.notification;

import club.tifality.Tifality;
import club.tifality.utils.handler.Manager;
import club.tifality.utils.render.LockedResolution;
import net.minecraft.client.gui.ScaledResolution;

import java.util.List;

public final class NotificationManager extends Manager<Notification> {
    public NotificationManager() {
        Tifality.getInstance().getEventBus().subscribe(this);
    }

    public void render(ScaledResolution scaledResolution,
                       LockedResolution lockedResolution,
                       boolean inGame,
                       int yOffset) {
        List<Notification> notifications = getElements();

        Notification remove = null;

        for (int i = 0; i < notifications.size(); i++) {
            Notification notification = notifications.get(i);

            if (notification.isDead()) {
                remove = notification;
                continue;
            }

            notification.render(lockedResolution, scaledResolution, i + 1, yOffset);
        }

        if (remove != null)
            getElements().remove(remove);
    }

    public void add(Notification notification) {
        getElements().add(notification);
    }

}
