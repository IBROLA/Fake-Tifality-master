package club.tifality.gui.screen;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import club.tifality.utils.render.RenderingUtils;

public class SplashProgress {
    private static final int MAX = 7;
    private static int PROGRESS = 0;
    private static ResourceLocation splash;

    public static void update() {
        if (Minecraft.getMinecraft() == null || Minecraft.getMinecraft().getLanguageManager() == null) {
            return;
        }
        SplashProgress.drawSplash(Minecraft.getMinecraft().getTextureManager());
    }

    public static void setProgress(int givenProgress) {
        PROGRESS = givenProgress;
        SplashProgress.update();
    }

    public static void drawSplash(TextureManager textureManager) {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        int scaleFactor = scaledResolution.getScaleFactor();
        Framebuffer framebuffer = new Framebuffer(scaledResolution.getScaledWidth() * scaleFactor, scaledResolution.getScaledHeight() * scaleFactor, true);
        framebuffer.bindFramebuffer(false);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0, scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight(), 0.0, 1000.0, 3000.0);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0f, 0.0f, -2000.0f);
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        GlStateManager.disableDepth();
        GlStateManager.enableTexture2D();
        if (splash == null) {
            splash = new ResourceLocation("tifality/launching.png");
        }
        textureManager.bindTexture(splash);
        GlStateManager.resetColor();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        Gui.drawScaledCustomSizeModalRect(0, 0, 0.0f, 0.0f, 1920, 1080, scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight(), 1920.0f, 1080.0f);
        SplashProgress.drawProgress();
        framebuffer.unbindFramebuffer();
        framebuffer.framebufferRender(scaledResolution.getScaledWidth() * scaleFactor, scaledResolution.getScaledHeight() * scaleFactor);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1f);
        Minecraft.getMinecraft().updateDisplay();
    }

    private static void drawProgress() {
        if (Minecraft.getMinecraft().gameSettings == null || Minecraft.getMinecraft().getTextureManager() == null) {
            return;
        }
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        double nProgress = PROGRESS;
        double calc = nProgress / 7.0 * ((double)sr.getScaledWidth() * 0.595);
        GlStateManager.resetColor();
        SplashProgress.resetTextureState();
        Gui.drawRect(84.0f, sr.getScaledHeight() - 110, sr.getScaledWidth() - 88, sr.getScaledHeight() - 95, new Color(0, 0, 0).getRGB());
        Gui.drawRect(86.0f, sr.getScaledHeight() - 108, sr.getScaledWidth() - 90, sr.getScaledHeight() - 97, new Color(50, 50, 50).getRGB());
        Gui.drawRect(86.0, sr.getScaledHeight() - 108, 86.0 + calc, sr.getScaledHeight() - 97, new Color(47, 200, 47).getRGB());
        double width1 = sr.getScaledWidth() - 90;
        for (int j = 1; j <= 6; ++j) {
            double dThing = width1 / 9.35 * (double)j;
            RenderingUtils.rectangle(86.0 + dThing, sr.getScaledHeight() - 108, 89.0 + dThing, sr.getScaledHeight() - 97, new Color(0, 0, 0).getRGB());
        }
    }

    private static void resetTextureState() {
        GlStateManager.textureState[GlStateManager.activeTextureUnit].textureName = -1;
    }
}

