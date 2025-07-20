package club.tifality.gui.notification.client;

import club.tifality.utils.render.Translate;
import club.tifality.utils.timer.Stopwatch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import club.tifality.gui.font.FontRenderer;

public final class Notification {
    public static final int HEIGHT = 30;
    private final String title;
    private final String content;
    private final int time;
    private final NotificationType type;
    private final Stopwatch timer;
    private final Translate translate;
    private final FontRenderer titleFont;
    private final FontRenderer infoFont;
    public double scissorBoxWidth;

    public Notification(String title, String content, NotificationType type, FontRenderer titleFont, FontRenderer infoFont, int ms) {
        this.title = title;
        this.content = content;
        this.time = ms;
        this.type = type;
        this.timer = new Stopwatch();
        this.titleFont = titleFont;
        this.infoFont = infoFont;
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        this.translate = new Translate(sr.getScaledWidth() - this.getWidth(), sr.getScaledHeight() - 30);
    }

    public final int getWidth() {
        return (int)Math.max(100.0f, Math.max(this.titleFont.getWidth(this.title), this.infoFont.getWidth(this.content)) + 24.0f);
    }

    public String getTitle() {
        return this.title;
    }

    public String getContent() {
        return this.content;
    }

    public int getTime() {
        return this.time;
    }

    public NotificationType getType() {
        return this.type;
    }

    public Stopwatch getTimer() {
        return this.timer;
    }

    public Translate getTranslate() {
        return this.translate;
    }
}

