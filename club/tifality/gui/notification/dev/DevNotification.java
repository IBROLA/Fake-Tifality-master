package club.tifality.gui.notification.dev;

import club.tifality.utils.render.Opacity;
import club.tifality.utils.render.Translate;

public class DevNotification implements IDevNotification {
    private final String text;
    private final long start;
    public int targetOpacity;
    public Translate translate;
    public Opacity opacity;

    public DevNotification(String text) {
        this.text = text;
        this.start = System.currentTimeMillis();
        this.translate = new Translate(2.0, 0.0);
        this.opacity = new Opacity(255);
        this.targetOpacity = 255;
    }

    public long checkTime() {
        return System.currentTimeMillis() - this.getDisplayTime();
    }

    @Override
    public String getMessage() {
        return this.text;
    }

    @Override
    public long getInitializeTime() {
        return this.start;
    }

    @Override
    public long getDisplayTime() {
        return 3000L;
    }
}

