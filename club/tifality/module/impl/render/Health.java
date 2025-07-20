package club.tifality.module.impl.render;

import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.event.impl.render.overlay.Render2DEvent;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@ModuleInfo(label = "Health", category = ModuleCategory.RENDER)
public class Health extends Module {
    private final DecimalFormat decimalFormat = new DecimalFormat("0.#", new DecimalFormatSymbols(Locale.ENGLISH));
    private int width;
    
    @Listener
    public void onRender2D(Render2DEvent event) {
        ScaledResolution sr = new ScaledResolution(mc);
        if (mc.thePlayer.getHealth() >= 0.0f && mc.thePlayer.getHealth() < 10.0f) {
            this.width = 3;
        }
        if (mc.thePlayer.getHealth() >= 10.0f && mc.thePlayer.getHealth() < 100.0f) {
            this.width = 3;
        }
        float health = mc.thePlayer.getHealth();
        float absorptionHealth = mc.thePlayer.getAbsorptionAmount();
        String absorp = (absorptionHealth <= 0.0f) ? "" : ("§e" + this.decimalFormat.format(absorptionHealth / 2.0f) + "§6❤");
        String string = this.decimalFormat.format(health / 2.0f) + "§c❤ " + absorp;
        int x = new ScaledResolution(mc).getScaledWidth() / 2 - this.width;
        int y = new ScaledResolution(mc).getScaledHeight() / 2 + 25;
        mc.fontRendererObj.drawString(string, (absorptionHealth > 0.0f) ? (x - 15.5f) : (x - 3.5f), (float)y, this.getHealthColor(), true);
        GL11.glPushAttrib(1048575);
        GL11.glPushMatrix();
        float i = 0.0f;
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        mc.getTextureManager().bindTexture(Gui.icons);
        while (i < mc.thePlayer.getMaxHealth() / 2.0f) {
            Gui.drawTexturedModalRect(sr.getScaledWidth() / 2.0f - mc.thePlayer.getMaxHealth() / 2.5f * 10.0f / 2.0f + i * 8.0f, sr.getScaledHeight() / 2.0f + 15.0f, 16, 0, 9, 9);
            ++i;
        }
        for (i = 0.0f; i < mc.thePlayer.getHealth() / 2.0f; ++i) {
            Gui.drawTexturedModalRect(sr.getScaledWidth() / 2.0f - mc.thePlayer.getMaxHealth() / 2.5f * 10.0f / 2.0f + i * 8.0f, sr.getScaledHeight() / 2.0f + 15.0f, 52, 0, 9, 9);
        }
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }
    
    private int getHealthColor() {
        if (mc.thePlayer.getHealth() <= 2.0f) {
            return new Color(255, 0, 0).getRGB();
        }
        if (mc.thePlayer.getHealth() <= 6.0f) {
            return new Color(255, 110, 0).getRGB();
        }
        if (mc.thePlayer.getHealth() <= 8.0f) {
            return new Color(255, 182, 0).getRGB();
        }
        if (mc.thePlayer.getHealth() <= 10.0f) {
            return new Color(255, 255, 0).getRGB();
        }
        if (mc.thePlayer.getHealth() <= 13.0f) {
            return new Color(255, 255, 0).getRGB();
        }
        if (mc.thePlayer.getHealth() <= 15.5f) {
            return new Color(182, 255, 0).getRGB();
        }
        if (mc.thePlayer.getHealth() <= 18.0f) {
            return new Color(108, 255, 0).getRGB();
        }
        if (mc.thePlayer.getHealth() <= 20.0f) {
            return new Color(0, 255, 0).getRGB();
        }
        return 0;
    }
}
