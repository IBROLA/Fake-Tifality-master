package club.tifality.module.impl.render;

import club.tifality.Tifality;
import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.event.impl.render.overlay.Render2DEvent;
import club.tifality.utils.render.Colors;
import club.tifality.utils.render.OGLUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import club.tifality.gui.notification.dev.DevNotifications;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.module.impl.combat.KillAura;
import club.tifality.property.Property;
import club.tifality.utils.Wrapper;
import club.tifality.utils.render.LockedResolution;
import club.tifality.utils.render.RenderingUtils;

import java.awt.*;
import java.text.DecimalFormat;

@ModuleInfo(label="Debug", category= ModuleCategory.RENDER)
public final class Debug extends Module {
    private final Property<Boolean> displayScreen = new Property<>("Display Screen", false);
    private final Property<Boolean> attackDebug = new Property<>("Attack Debug", true);
    private final ResourceLocation BACKGROUND_IMAGE = new ResourceLocation("tifality/skeetchainmail.png");

    public Debug() {
        this.setHidden(true);
    }

    @Listener
    public void onRenderOverlay(Render2DEvent event) {
        LockedResolution resolution = event.getResolution();
        float x = (float)resolution.getWidth() / 2.0f;
        float y = (float)resolution.getHeight() / 2.0f;
        float half = 160.0f;
        float left = x - half + 3.5f;
        float top = y - 150.0f + 3.5f;
        float right = x + half - 3.5f;
        float bottom = y - 75.0f - 3.5f;
        if (this.displayScreen.get()) {
            Gui.drawRect(x - half, y - 150.0f, x + half, y - 75.0f, new Color(10, 10, 10).getRGB());
            Gui.drawRect(x - half + 0.5f, y - 150.0f + 0.5f, x + half - 0.5f, y - 75.0f - 0.5f, new Color(60, 60, 60).getRGB());
            Gui.drawRect(x - half + 1.0f, y - 150.0f + 1.0f, x + half - 1.0f, y - 75.0f - 1.0f, new Color(40, 40, 40).getRGB());
            Gui.drawRect(x - half + 3.0f, y - 150.0f + 3.0f, x + half - 3.0f, y - 75.0f - 3.0f, new Color(47, 47, 47).getRGB());
            Gui.drawRect(left, top, right, bottom, new Color(21, 21, 21).getRGB());
            GL11.glEnable(3089);
            OGLUtils.startScissorBox(event.getResolution(), (int)left, (int)top, (int)(right - left), (int)(bottom - top));
            RenderingUtils.drawImage(left, top, 325.0f, 275.0f, 1.0f, 1.0f, 1.0f, this.BACKGROUND_IMAGE);
            RenderingUtils.drawImage(left + 325.0f, top + 1.0f, 325.0f, 275.0f, 1.0f, 1.0f, 1.0f, this.BACKGROUND_IMAGE);
            RenderingUtils.drawImage(left + 1.0f, top + 275.0f, 325.0f, 275.0f, 1.0f, 1.0f, 1.0f, this.BACKGROUND_IMAGE);
            RenderingUtils.drawImage(left + 326.0f, top + 276.0f, 325.0f, 275.0f, 1.0f, 1.0f, 1.0f, this.BACKGROUND_IMAGE);
            GL11.glDisable(3089);
            Gui.drawRect(x - half + 8.0f, y - 150.0f + 8.0f, x + half - 8.0f, y - 75.0f - 8.0f, new Color(10, 10, 10).getRGB());
            Gui.drawRect(x - half + 8.5f, y - 150.0f + 8.5f, x + half - 8.5f, y - 75.0f - 8.5f, new Color(48, 48, 48).getRGB());
            Gui.drawRect(x - half + 9.0f, y - 150.0f + 9.0f, x + half - 9.0f, y - 75.0f - 9.0f, new Color(23, 23, 23).getRGB());
            Wrapper.getCSGOFontRenderer().drawString("tifa§Ality", x - half + 15.0f, y - 150.0f + 8.0f, new Color(255, 255, 255).getRGB(), true);
            if (!Tifality.getSourceConsoleGUI().sourceConsole.getStringList().isEmpty()) {
                GlStateManager.pushMatrix();
                this.prepareScissorBox(x - half + 9.0f, y - 150.0f + 12.5f, x + half - 9.0f, y - 75.0f - 9.0f);
                GL11.glEnable(3089);
                float yMEME = 0.0f;
                float maximum = (y - 150.0f + 16.5f) / 6.0f - 3.5f;
                if (Tifality.getSourceConsoleGUI().sourceConsole.getStringList().size() > (int)maximum) {
                    yMEME = ((float) Tifality.getSourceConsoleGUI().sourceConsole.getStringList().size() - maximum) * -6.0f;
                }
                for (String str : Tifality.getSourceConsoleGUI().sourceConsole.getStringList()) {
                    Wrapper.getCSGOFontRenderer().drawString("§A[tifality]§R " + str, x - half + 11.0f, y - 150.0f + 11.0f + yMEME, -1);
                    yMEME += 6.0f;
                }
                GL11.glDisable(3089);
                GlStateManager.popMatrix();
            }
            float xDif = (right - left) / 2.0f;
            RenderingUtils.drawGradientRect(left += 0.5f, top += 0.5f, left + xDif, top + 1.5f - 0.5f, true, Colors.getColor(55, 177, 218), Colors.getColor(204, 77, 198));
            RenderingUtils.drawGradientRect(left + xDif, top, right -= 0.5f, top + 1.5f - 0.5f, true, Colors.getColor(204, 77, 198), Colors.getColor(204, 227, 53));
        }
        DecimalFormat yes = new DecimalFormat("0");
        DecimalFormat decimalFormat = new DecimalFormat("0.0#");
        int damage = (int)(KillAura.getInstance().getTarget().getMaxHealth() - KillAura.getInstance().getTarget().getHealth());
        if (this.attackDebug.get() && KillAura.getInstance().getTarget() != null) {
            DevNotifications.getManager().post("Hit " + KillAura.getInstance().getTarget().getName() + " for " + decimalFormat.format(damage) + " damage (" + decimalFormat.format(KillAura.getInstance().getTarget().getHealth()) + " health remaining) X:" + yes.format(KillAura.getInstance().getTarget().posX) + " Y: " + yes.format(KillAura.getInstance().getTarget().posY) + " Z: " + yes.format(KillAura.getInstance().getTarget().posZ));
        }
    }

    private void prepareScissorBox(float x, float y, float x2, float y2) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scale = new ScaledResolution(mc);
        int factor = scale.getScaleFactor();
        GL11.glScissor((int)(x * (float)factor), (int)(((float)scale.getScaledHeight() - y2) * (float)factor), (int)((x2 - x) * (float)factor), (int)((y2 - y) * (float)factor));
    }
}

