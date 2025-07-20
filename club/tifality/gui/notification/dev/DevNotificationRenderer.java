package club.tifality.gui.notification.dev;

import java.awt.Color;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class DevNotificationRenderer implements IDevNotificationRenderer {
    @Override
    public void draw(List<DevNotification> notifications) {
        int y = 3;
        for (DevNotification notification : notifications) {
            notification.opacity.interpolate(notification.targetOpacity);
            notification.translate.interpolate(60.0f, y, 0.35f);
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5, 0.5, 0.5);
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(notification.getMessage(), 5.0f, (float)notification.translate.getY(), new Color(255, 255, 255, (int)notification.opacity.getOpacity()).getRGB());
            GlStateManager.popMatrix();
            y += 8;
            if (notification.checkTime() < notification.getDisplayTime() + notification.getInitializeTime()) continue;
            notification.targetOpacity = 0;
            if (!(notification.opacity.getOpacity() <= 0.0f)) continue;
            notifications.remove(notification);
        }
    }
}

