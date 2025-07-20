package club.tifality.gui.notification.client;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import club.tifality.utils.Wrapper;
import club.tifality.utils.render.AnimationUtil;
import club.tifality.utils.render.Colors;
import club.tifality.utils.render.Translate;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import club.tifality.gui.font.TrueTypeFontRenderer;
import club.tifality.utils.render.RenderingUtils;

public final class NotificationPublisher {
    private static final List<Notification> NOTIFICATIONS = new CopyOnWriteArrayList<>();

    public static void publish(ScaledResolution sr) {
        if (NOTIFICATIONS.isEmpty()) {
            return;
        }
        int srScaledHeight = sr.getScaledHeight();
        int scaledWidth = sr.getScaledWidth();
        int y = srScaledHeight - 30;
        for (Notification notification : NOTIFICATIONS) {
            Translate translate = notification.getTranslate();
            int width = notification.getWidth();
            if (!notification.getTimer().elapsed(notification.getTime())) {
                notification.scissorBoxWidth = AnimationUtil.animate(width, notification.scissorBoxWidth, 0.05);
                translate.interpolate(scaledWidth - width, y, 0.15);
            } else {
                notification.scissorBoxWidth = AnimationUtil.animate(0.0, notification.scissorBoxWidth, 0.05);
                if (notification.scissorBoxWidth < 1.0) {
                    NOTIFICATIONS.remove(notification);
                }
                y += 24;
            }
            float translateX = (float)translate.getX();
            float translateY = (float)translate.getY();
            GL11.glPushMatrix();
            GL11.glEnable(3089);
            RenderingUtils.prepareScissorBox((float)((double)scaledWidth - notification.scissorBoxWidth), translateY, scaledWidth, translateY + 30.0f);
            Gui.drawRect(translateX, translateY + 7.0f, scaledWidth, translateY + 30.5f, Colors.getColor(0, 200));
            Gui.drawRect(translateX, translateY + 30.0f - 1.5f, scaledWidth, translateY + 30.0f, RenderingUtils.darker(notification.getType().getColor(), 0.4f));
            Gui.drawRect(translateX, translateY + 30.0f - 1.5f, translateX + (float)width * (float)((long)notification.getTime() - notification.getTimer().getElapsedTime()) / (float)notification.getTime(), translateY + 30.0f, notification.getType().getColor());
            Wrapper.getTitleFont().drawStringWithShadow(notification.getTitle(), translateX + 21.5f, translateY + 8.5f, -1);
            Wrapper.getInfoFont().drawStringWithShadow(notification.getContent(), translateX + 21.5f, translateY + 19.5f, -1);
            RenderingUtils.drawImage(new ResourceLocation("tifality/" + notification.getType().name().toLowerCase() + ".png"), translateX + 2.0f, translateY + 8.5f, 18, 18);
            GL11.glDisable(3089);
            GL11.glPopMatrix();
            y -= 24;
        }
    }

    public static void queue(String title, String content, NotificationType type, int ms) {
        TrueTypeFontRenderer titleFont = Wrapper.getTitleFont();
        TrueTypeFontRenderer infoFont = Wrapper.getInfoFont();
        NOTIFICATIONS.add(new Notification(title, content, type, titleFont, infoFont, ms));
    }
}

