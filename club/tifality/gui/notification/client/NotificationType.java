package club.tifality.gui.notification.client;

import java.awt.Color;

public enum NotificationType {
    OKAY(new Color(65, 252, 65).getRGB()),
    INFO(new Color(127, 174, 210).getRGB()),
    NOTIFY(new Color(255, 255, 94).getRGB()),
    WARNING(new Color(226, 87, 76).getRGB());

    private final int color;

    NotificationType(int color) {
        this.color = color;
    }

    public final int getColor() {
        return this.color;
    }
}

