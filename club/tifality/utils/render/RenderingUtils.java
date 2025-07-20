package club.tifality.utils.render;

import club.tifality.Tifality;
import club.tifality.gui.font.FontRenderer;
import club.tifality.gui.font.TrueTypeFontRenderer;
import club.tifality.utils.MathUtils;
import club.tifality.utils.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Timer;
import net.minecraft.util.Vec3;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Cylinder;
import club.tifality.module.impl.combat.KillAura;
import org.lwjgl.util.glu.GLU;

import java.awt.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.regex.Pattern;

import static org.lwjgl.opengl.GL11.*;

public final class RenderingUtils {

    private static final double DOUBLE_PI = Math.PI * 2.0D;

    private static final Frustum FRUSTUM = new Frustum();
    public static final Pattern COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");;

    private static int lastScaledWidth;
    private static int lastScaledHeight;
    private static int lastGuiScale;
    private static ScaledResolution scaledResolution;

    private static int lastWidth;
    private static int lastHeight;
    private static LockedResolution lockedResolution;

    private RenderingUtils() {
    }

    // Sometimes colors get messed up in for loops, so we use this method to reset it to allow new colors to be used
    public static void resetColor() {
        GlStateManager.color(1, 1, 1, 1);
    }

    public static boolean isBBInFrustum(AxisAlignedBB aabb) {
        EntityPlayerSP player = Wrapper.getPlayer();
        FRUSTUM.setPosition(player.posX, player.posY, player.posZ);
        return FRUSTUM.isBoundingBoxInFrustum(aabb);
    }

    public static void drawRainbowString(String s, int x, int y) {
        int updateX = x;
        for (int i = 0; i < s.length(); ++i) {
            String str = "" + s.charAt(i);
            Minecraft.getMinecraft().fontRendererObj.drawString(str, updateX, y, asdzxc(i * 50, 20, 214).getRGB(), true);
            updateX += Minecraft.getMinecraft().fontRendererObj.getCharWidth(s.charAt(i));
        }
    }

    public static Color asdzxc(int delay, int offset, int index) {
        double asdsadzxcb = Math.ceil((double)(System.currentTimeMillis() + delay * (long)index)) / offset * 2.0;
        final long color = Long.parseLong(Integer.toHexString(Color.HSBtoRGB(((float)((asdsadzxcb %= 360.0) / 360.0) < 0.5) ? (-(float)(asdsadzxcb / 360.0)) : ((float)(asdsadzxcb / 360.0)), 0.55f, 0.95f)), 16);
        final Color c = new Color((int)color);
        return new Color(c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f, c.getAlpha() / 255.0f);
    }

    public static void drawPlatform(Entity entity, Color color) {
        Timer timer = Minecraft.getMinecraft().timer;
        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks - RenderManager.renderPosX;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks - RenderManager.renderPosY;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks - RenderManager.renderPosZ;
        AxisAlignedBB axisAlignedBB = entity.getEntityBoundingBox().offset(-entity.posX, -entity.posY, -entity.posZ).offset(x, y, z);
        drawAxisAlignedBB(new AxisAlignedBB(axisAlignedBB.minX - 0.1, axisAlignedBB.minY - 0.1, axisAlignedBB.minZ - 0.1, axisAlignedBB.maxX + 0.1, axisAlignedBB.maxY + 0.2, axisAlignedBB.maxZ + 0.1), color);
    }

    public static void drawAuraMark(Entity entity, Color color) {
        KillAura killaura = Tifality.getInstance().getModuleManager().getModule(KillAura.class);
        Timer timer = Minecraft.getMinecraft().timer;
        if (killaura == null) {
            return;
        }
        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks - RenderManager.renderPosX;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks - RenderManager.renderPosY;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks - RenderManager.renderPosZ;
        AxisAlignedBB axisAlignedBB = entity.getEntityBoundingBox().offset(-entity.posX, -entity.posY, -entity.posZ).offset(x, y - 0.41, z);
        drawAxisAlignedBB(new AxisAlignedBB(axisAlignedBB.minX, axisAlignedBB.maxY + 0.2, axisAlignedBB.minZ, axisAlignedBB.maxX, axisAlignedBB.maxY + 0.26, axisAlignedBB.maxZ), color);
    }

    public static void drawAxisAlignedBB(AxisAlignedBB axisAlignedBB, Color color) {
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glLineWidth(2.0f);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        glColor(color);
        drawFilledBox(axisAlignedBB);
        GlStateManager.resetColor();
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
    }

    public static void drawFilledBox(AxisAlignedBB axisAlignedBB) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        tessellator.draw();
    }

    public static Vec3 to2D(double x, double y, double z) {
        FloatBuffer screenCoords = BufferUtils.createFloatBuffer(3);
        IntBuffer viewport = BufferUtils.createIntBuffer(16);
        FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
        FloatBuffer projection = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(2982, modelView);
        GL11.glGetFloat(2983, projection);
        GL11.glGetInteger(2978, viewport);
        boolean result = GLU.gluProject((float)x, (float)y, (float)z, modelView, projection, viewport, screenCoords);
        if (result) {
            return new Vec3(screenCoords.get(0), (float)Display.getHeight() - screenCoords.get(1), screenCoords.get(2));
        }
        return null;
    }

    public static void drawTracerPointer(float x, float y, float size, float widthDiv, float heightDiv, int color) {
        boolean blend = GL11.glIsEnabled(3042);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glPushMatrix();
        hexColor(color);
        GL11.glBegin(7);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x - size / widthDiv, y + size);
        GL11.glVertex2d(x, y + size / heightDiv);
        GL11.glVertex2d(x + size / widthDiv, y + size);
        GL11.glVertex2d(x, y);
        GL11.glEnd();
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.8f);
        GL11.glBegin(2);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x - size / widthDiv, y + size);
        GL11.glVertex2d(x, y + size / heightDiv);
        GL11.glVertex2d(x + size / widthDiv, y + size);
        GL11.glVertex2d(x, y);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        if (!blend) {
            GL11.glDisable(3042);
        }
        GL11.glDisable(2848);
    }

    public static void hexColor(int hexColor) {
        float red = (float)(hexColor >> 16 & 0xFF) / 255.0f;
        float green = (float)(hexColor >> 8 & 0xFF) / 255.0f;
        float blue = (float)(hexColor & 0xFF) / 255.0f;
        float alpha = (float)(hexColor >> 24 & 0xFF) / 255.0f;
        GL11.glColor4f(red, green, blue, alpha);
    }

    public static void drawGradientRect(double left, double top, double right, double bottom,
                                        boolean sideways,
                                        int startColor, int endColor) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OGLUtils.enableBlending();
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glBegin(GL11.GL_QUADS);

        OGLUtils.color(startColor);
        if (sideways) {
            GL11.glVertex2d(left, top);
            GL11.glVertex2d(left, bottom);
            OGLUtils.color(endColor);
            GL11.glVertex2d(right, bottom);
            GL11.glVertex2d(right, top);
        } else {
            GL11.glVertex2d(left, top);
            OGLUtils.color(endColor);
            GL11.glVertex2d(left, bottom);
            GL11.glVertex2d(right, bottom);
            OGLUtils.color(startColor);
            GL11.glVertex2d(right, top);
        }

        GL11.glEnd();
        GL11.glDisable(GL_BLEND);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    public static LockedResolution getLockedResolution() {
        int width = Display.getWidth();
        int height = Display.getHeight();

        if (width != lastWidth ||
            height != lastHeight) {
            lastWidth = width;
            lastHeight = height;
            return lockedResolution = new LockedResolution(width / LockedResolution.SCALE_FACTOR, height / LockedResolution.SCALE_FACTOR);
        }

        return lockedResolution;
    }

    public static ScaledResolution getScaledResolution() {
        int displayWidth = Display.getWidth();
        int displayHeight = Display.getHeight();
        int guiScale = Wrapper.getGameSettings().guiScale;

        if (displayWidth != lastScaledWidth ||
            displayHeight != lastScaledHeight ||
            guiScale != lastGuiScale) {
            lastScaledWidth = displayWidth;
            lastScaledHeight = displayHeight;
            lastGuiScale = guiScale;
            return scaledResolution = new ScaledResolution(Wrapper.getMinecraft());
        }

        return scaledResolution;
    }

    public static int getColorFromPercentage(float percentage) {
        return Color.HSBtoRGB(Math.min(1.0F, Math.max(0.0F, percentage)) / 3, 0.9F, 0.9F);
    }

    public static int getRainbowFromEntity(long currentMillis, int speed, int offset, boolean invert, float alpha) {
        float time = ((currentMillis + (offset * 300L)) % speed) / (float) speed;
        int rainbow = Color.HSBtoRGB(invert ? 1.0F - time : time, 0.9F, 0.9F);
        int r = (rainbow >> 16) & 0xFF;
        int g = (rainbow >> 8) & 0xFF;
        int b = rainbow & 0xFF;
        int a = (int) (alpha * 255.0F);
        return ((a & 0xFF) << 24) |
            ((r & 0xFF) << 16) |
            ((g & 0xFF) << 8) |
            (b & 0xFF);
    }

    public static int getRainbow(int speed, int offset) {
        return Color.HSBtoRGB((float)((System.currentTimeMillis() + (offset * 100L)) % (long)speed) / (float)speed, 0.55f, 0.9f);
    }

    public static int getRainbow(int speed, int width, int offset) {
        long speedTime = (long)width * 1000L;
        float time = (float)((System.currentTimeMillis() + ((long) offset * speed)) % speedTime) / ((float)speedTime / 2.0f);
        return Color.HSBtoRGB(time, 0.55f, 0.9f);
    }

    public static void TScylinder1(Entity player, double x, double y, double z, double range, int s, float smoothLine, int color) {
        Cylinder c = new Cylinder();
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        GL11.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
        c.setDrawStyle(100011);
        GlStateManager.resetColor();
        glColor(color);
        enableSmoothLine(smoothLine);
        c.draw((float)(range + 0.25), (float)(range + 0.25), 0.0f, s, 0);
        c.draw((float)(range + 0.25), (float)(range + 0.25), 0.0f, s, 0);
        disableSmoothLine();
        GL11.glPopMatrix();
    }

    public static void TScylinder2(Entity player, double x, double y, double z, double range, float smoothLine, int s, int color) {
        Cylinder c = new Cylinder();
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        GL11.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
        c.setDrawStyle(100011);
        GlStateManager.resetColor();
        glColor(color);
        enableSmoothLine(smoothLine);
        c.draw((float)(range + 0.25), (float)(range + 0.25), 0.0f, s, 0);
        c.draw((float)(range + 0.25), (float)(range + 0.25), 0.0f, s, 0);
        disableSmoothLine();
        GL11.glPopMatrix();
    }

    public static void drawAndRotateArrow(float x, float y, float size, boolean rotate) {
        glPushMatrix();
        glTranslatef(x, y, 1.0F);
        OGLUtils.enableBlending();
        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        glLineWidth(1.0F);
        glDisable(GL_TEXTURE_2D);
        glBegin(GL_TRIANGLES);
        if (rotate) {
            glVertex2f(size, size / 2);
            glVertex2f(size / 2, 0);
            glVertex2f(0, size / 2);
        } else {
            glVertex2f(0, 0);
            glVertex2f(size / 2, size / 2);
            glVertex2f(size, 0);
        }
        glEnd();
        glEnable(GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
        glPopMatrix();
    }

    public static double progressiveAnimation(double now, double desired, double speed) {
        double dif = Math.abs(now - desired);

        final int fps = Minecraft.getDebugFPS();

        if (dif > 0) {
            double animationSpeed = MathUtils.roundToDecimalPlace(Math.min(
                10.0D, Math.max(0.05D, (144.0D / fps) * (dif / 10) * speed)), 0.05D);

            if (dif != 0 && dif < animationSpeed)
                animationSpeed = dif;

            if (now < desired)
                return now + animationSpeed;
            else if (now > desired)
                return now - animationSpeed;
        }

        return now;
    }

    public static double linearAnimation(double now, double desired, double speed) {
        double dif = Math.abs(now - desired);

        final int fps = Minecraft.getDebugFPS();

        if (dif > 0) {
            double animationSpeed = MathUtils.roundToDecimalPlace(Math.min(
                10.0D, Math.max(0.005D, (144.0D / fps) * speed)), 0.005D);

            if (dif != 0 && dif < animationSpeed)
                animationSpeed = dif;

            if (now < desired)
                return now + animationSpeed;
            else if (now > desired)
                return now - animationSpeed;
        }

        return now;
    }

    public static int alphaComponent(int color, int alphaComp) {
        final int r = (color >> 16 & 0xFF);
        final int g = (color >> 8 & 0xFF);
        final int b = (color & 0xFF);

        return ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8) |
                (b & 0xFF) |
                ((alphaComp & 0xFF) << 24);
    }

    public static int darkerClamped(int color, float factor) {
        int r = (int) Math.max(0, Math.min(255, (color >> 16 & 0xFF) * factor));
        int g = (int) Math.max(0, Math.min(255, (color >> 8 & 0xFF) * factor));
        int b = (int) Math.max(0, Math.min(255, (color & 0xFF) * factor));
        int a = Math.max(0, Math.min(255, color >> 24 & 0xFF));


        return ((r & 0xFF) << 16) |
            ((g & 0xFF) << 8) |
            (b & 0xFF) |
            ((a & 0xFF) << 24);
    }

    public static int darker(final int color, final float factor) {
        final int r = (int) ((color >> 16 & 0xFF) * factor);
        final int g = (int) ((color >> 8 & 0xFF) * factor);
        final int b = (int) ((color & 0xFF) * factor);
        final int a = color >> 24 & 0xFF;

        return ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8) |
                (b & 0xFF) |
                ((a & 0xFF) << 24);
    }


    public static int darker(int color) {
        return darker(color, 0.6F);
    }

    public static void drawOutlinedString(FontRenderer fr, String s, float x, float y, int color, int outlineColor) {
        fr.drawString(s, x - 0.5F, y, outlineColor);
        fr.drawString(s, x, y - 0.5F, outlineColor);
        fr.drawString(s, x + 0.5F, y, outlineColor);
        fr.drawString(s, x, y + 0.5F, outlineColor);
        fr.drawString(s, x, y, color);
    }

    public static void drawOutlinedString(FontRenderer fr, String s, float x, float y, float width, int color, int outlineColor) {
        fr.drawString(s, x - width, y, outlineColor);
        fr.drawString(s, x, y - width, outlineColor);
        fr.drawString(s, x + width, y, outlineColor);
        fr.drawString(s, x, y + width, outlineColor);
        fr.drawString(s, x, y, color);
    }

    public static void drawOutlineString(TrueTypeFontRenderer fr, String s, float x, float y, int color, int outlineColor) {
        fr.drawString(stripColor(s), x - 0.5f, y, outlineColor);
        fr.drawString(stripColor(s), x + 0.5f, y, outlineColor);
        fr.drawString(stripColor(s), x, y + 0.5f, outlineColor);
        fr.drawString(stripColor(s), x, y - 0.5f, outlineColor);
        fr.drawString(s, x, y, color);
    }

    public static void drawOutlineString(TrueTypeFontRenderer fr, String s, float x, float y, float width, int color, int outlineColor) {
        fr.drawString(stripColor(s), x - width, y, outlineColor);
        fr.drawString(stripColor(s), x + width, y, outlineColor);
        fr.drawString(stripColor(s), x, y + width, outlineColor);
        fr.drawString(stripColor(s), x, y - width, outlineColor);
        fr.drawString(s, x, y, color);
    }

    public static void drawOutlinedString(String s, float x, float y, int color, int outlineColor) {
        Minecraft.getMinecraft().fontRendererObj.drawString(s, x - 0.5F, y, outlineColor);
        Minecraft.getMinecraft().fontRendererObj.drawString(s, x, y - 0.5F, outlineColor);
        Minecraft.getMinecraft().fontRendererObj.drawString(s, x + 0.5F, y, outlineColor);
        Minecraft.getMinecraft().fontRendererObj.drawString(s, x, y + 0.5F, outlineColor);
        Minecraft.getMinecraft().fontRendererObj.drawString(s, x, y, color);
    }

    public static void drawOutlinedString(String s, float x, float y, float width, int color, int outlineColor) {
        Minecraft.getMinecraft().fontRendererObj.drawString(s, x - width, y, outlineColor);
        Minecraft.getMinecraft().fontRendererObj.drawString(s, x, y - width, outlineColor);
        Minecraft.getMinecraft().fontRendererObj.drawString(s, x + width, y, outlineColor);
        Minecraft.getMinecraft().fontRendererObj.drawString(s, x, y + width, outlineColor);
        Minecraft.getMinecraft().fontRendererObj.drawString(s, x, y, color);
    }

    public static String stripColor(String input) {
        return COLOR_PATTERN.matcher(input).replaceAll("");
    }

    public static void drawImage(float x, float y, float width, float height, float r, float g, float b, ResourceLocation image) {
        Wrapper.getMinecraft().getTextureManager().bindTexture(image);
        float f = 1.0F / width;
        float f1 = 1.0F / height;
        glColor4f(r, g, b, 1.0F);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0D)
                .tex(0.0D, height * f1)
                .endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D)
                .tex(width * f, height * f1)
                .endVertex();
        worldrenderer.pos(x + width, y, 0.0D)
                .tex(width * f, 0.0D)
                .endVertex();
        worldrenderer.pos(x, y, 0.0D)
                .tex(0.0D, 0.0D)
                .endVertex();
        tessellator.draw();
    }

    public static void drawImage(ResourceLocation image, int x, int y, int width, int height) {
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDepthMask(false);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, width, height, (float)width, (float)height);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
    }

    public static void drawImage(ResourceLocation image, float x, float y, int width, int height) {
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDepthMask(false);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        float f = 1.0F / (float)width;
        float f1 = 1.0F / (float)height;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + (float)height, 0.0).tex(0.0F * f, (float)height * f1).endVertex();
        worldrenderer.pos(x + (float)width, y + (float)height, 0.0)
                .tex((float)width * f, (float)height * f1)
                .endVertex();
        worldrenderer.pos(x + (float)width, y, 0.0).tex((float)width * f, 0.0F * f1).endVertex();
        worldrenderer.pos(x, y, 0.0).tex(0.0F * f, 0.0F * f1).endVertex();
        tessellator.draw();
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
    }

    public static void drawRoundedRect(float x, float y, float x1, float y1, int borderC, int insideC) {
        drawRect(x + 0.5f, y, x1 - 0.5f, y + 0.5f, insideC);
        drawRect(x + 0.5f, y1 - 0.5f, x1 - 0.5f, y1, insideC);
        drawRect(x, y + 0.5f, x1, y1 - 0.5f, insideC);
    }

    public static void drawRect(float x, float y, float x1, float y1, int color) {
        enableGL2D();
        glColor(color);
        drawRect(x, y, x1, y1);
        disableGL2D();
    }

    public static void drawRect(float x, float y, float x1, float y1) {
        GL11.glBegin(7);
        GL11.glVertex2f(x, y1);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x1, y);
        GL11.glVertex2f(x, y);
        GL11.glEnd();
    }

    public static void glColor(int hex) {
        float alpha = (float)(hex >> 24 & 0xFF) / 255.0f;
        float red = (float)(hex >> 16 & 0xFF) / 255.0f;
        float green = (float)(hex >> 8 & 0xFF) / 255.0f;
        float blue = (float)(hex & 0xFF) / 255.0f;
        GL11.glColor4f(red, green, blue, alpha);
    }

    public static void glColor(Color color) {
        float red = (float)color.getRed() / 255.0f;
        float green = (float)color.getGreen() / 255.0f;
        float blue = (float)color.getBlue() / 255.0f;
        float alpha = (float)color.getAlpha() / 255.0f;
        GlStateManager.color(red, green, blue, alpha);
    }

    public static void enableGL2D() {
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glDepthMask(true);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
    }

    public static void disableGL2D() {
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
    }

    public static void rectangle(double left, double top, double right, double bottom, int color) {
        double angle;
        if (left < right) {
            angle = left;
            left = right;
            right = angle;
        }
        if (top < bottom) {
            angle = top;
            top = bottom;
            bottom = angle;
        }
        float alpha = (float) (color >> 24 & 255) / 255.0f;
        float red = (float) (color >> 16 & 255) / 255.0f;
        float green = (float) (color >> 8 & 255) / 255.0f;
        float blue = (float) (color & 255) / 255.0f;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(red, green, blue, alpha);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(left, bottom, 0.0).endVertex();
        worldRenderer.pos(right, bottom, 0.0).endVertex();
        worldRenderer.pos(right, top, 0.0).endVertex();
        worldRenderer.pos(left, top, 0.0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void rectangleBordered(double x, double y, double x1, double y1, double width, int internalColor, int borderColor) {
        rectangle(x + width, y + width, x1 - width, y1 - width, internalColor);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        rectangle(x + width, y, x1 - width, y + width, borderColor);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        rectangle(x, y, x + width, y1, borderColor);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        rectangle(x1 - width, y, x1, y1, borderColor);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        rectangle(x + width, y1 - width, x1 - width, y1, borderColor);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static int fadeBetween(int startColor, int endColor, float progress) {
        if (progress > 1)
            progress = 1 - progress % 1;

        return fadeTo(startColor, endColor, progress);
    }

    public static int fadeBetween(int startColor, int endColor) {
        return fadeBetween(startColor, endColor, (System.currentTimeMillis() % 2000) / 1000.0F);
    }

    public static int fadeTo(int startColor, int endColor, float progress) {
        float invert = 1.0F - progress;
        int r = (int) ((startColor >> 16 & 0xFF) * invert +
                (endColor >> 16 & 0xFF) * progress);
        int g = (int) ((startColor >> 8 & 0xFF) * invert +
                (endColor >> 8 & 0xFF) * progress);
        int b = (int) ((startColor & 0xFF) * invert +
                (endColor & 0xFF) * progress);
        int a = (int) ((startColor >> 24 & 0xFF) * invert +
                (endColor >> 24 & 0xFF) * progress);
        return ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8) |
                (b & 0xFF);
    }

    public static void drawLoop(float x,
                                float y,
                                double radius,
                                int points,
                                float width,
                                int color,
                                boolean filled) {
        glDisable(GL_TEXTURE_2D);
        glLineWidth(width);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        OGLUtils.color(color);
        int smooth = filled ? GL_POLYGON_SMOOTH : GL_LINE_SMOOTH;
        glEnable(smooth);
        glHint(filled ? GL_POLYGON_SMOOTH_HINT : GL_LINE_SMOOTH_HINT, GL_NICEST);
        glBegin(filled ? GL_TRIANGLE_FAN : GL_LINE_LOOP);
        for (int i = 0; i < points; i++) {
            if (filled) {
                final double cs = i * Math.PI / 180;
                final double ps = (i - 1) * Math.PI / 180;

                glVertex2d(x + Math.cos(ps) * radius, y + -Math.sin(ps) * radius);
                glVertex2d(x + Math.cos(cs) * radius, y + -Math.sin(cs) * radius);
                glVertex2d(x, y);
            } else {
                glVertex2d(x + radius * Math.cos(i * DOUBLE_PI / points),
                        y + radius * Math.sin(i * DOUBLE_PI / points));
            }
        }
        glEnd();
        glDisable(smooth);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
    }

    public static Vec3 getEntityRenderPosition(Entity entity) {
        return new Vec3(getEntityRenderX(entity), getEntityRenderY(entity), getEntityRenderZ(entity));
    }

    public static double getEntityRenderX(Entity entity) {
        return entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * Minecraft.getMinecraft().timer.renderPartialTicks - RenderManager.renderPosX;
    }

    public static double getEntityRenderY(Entity entity) {
        return entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * Minecraft.getMinecraft().timer.renderPartialTicks - RenderManager.renderPosY;
    }

    public static double getEntityRenderZ(Entity entity) {
        return entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * Minecraft.getMinecraft().timer.renderPartialTicks - RenderManager.renderPosZ;
    }

//    public static void drawLinesAroundPlayer(Entity entity,
//                                             double radius,
//                                             float partialTicks,
//                                             int points,
//                                             boolean outline,
//                                             int color) {
//        glPushMatrix();
//        glDisable(GL_TEXTURE_2D);
//        glEnable(GL_LINE_SMOOTH);
//        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
//        glDisable(GL_DEPTH_TEST);
//        glEnable(GL_BLEND);
//        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
//        glDisable(GL_DEPTH_TEST);
//        final double x = interpolate(entity.prevPosX, entity.posX, partialTicks) - RenderManager.viewerPosX;
//        final double y = interpolate(entity.prevPosY, entity.posY, partialTicks) - RenderManager.viewerPosY;
//        final double z = interpolate(entity.prevPosZ, entity.posZ, partialTicks) - RenderManager.viewerPosZ;
//
//        if (outline) {
//            glLineWidth(6.0F);
//            OGLUtils.color(0x80000000);
//
//            glBegin(GL_LINE_STRIP);
//            for (int i = 0; i <= points; i++)
//                glVertex3d(
//                        x + radius * Math.cos(i * DOUBLE_PI / points),
//                        y,
//                        z + radius * Math.sin(i * DOUBLE_PI / points));
//            glEnd();
//        }
//
//        glLineWidth(3.0F);
//        OGLUtils.color(color);
//
//        glBegin(GL_LINE_STRIP);
//        for (int i = 0; i <= points; i++)
//            glVertex3d(
//                    x + radius * Math.cos(i * DOUBLE_PI / points),
//                    y,
//                    z + radius * Math.sin(i * DOUBLE_PI / points));
//        glEnd();
//        glDepthMask(true);
//        glDisable(GL_BLEND);
//        glEnable(GL_DEPTH_TEST);
//        glDisable(GL_LINE_SMOOTH);
//        glEnable(GL_DEPTH_TEST);
//        glEnable(GL_TEXTURE_2D);
//        glPopMatrix();
//    }

    public static void color(int color) {
        GL11.glColor4ub((byte)(color >> 16 & 0xFF), (byte)(color >> 8 & 0xFF), (byte)(color & 0xFF), (byte)(color >> 24 & 0xFF));
    }

    public static double interpolate(double old,
                                     double now,
                                     float partialTicks) {
        return old + (now - old) * partialTicks;
    }

    public static float interpolate(float old,
                                    float now,
                                    float partialTicks) {

        return old + (now - old) * partialTicks;
    }

    public static void drawGuiBackground(int width, int height) {
        Gui.drawRect(0, 0, width, height, 0xFF282C34);
    }

    private static String getColor(int n) {
        if (n != 1) {
            if (n == 2) {
                return "§a";
            }
            if (n == 3) {
                return "§3";
            }
            if (n == 4) {
                return "§4";
            }
            if (n >= 5) {
                return "§e";
            }
        }
        return "§f";
    }

    public static void renderEnchantText(ItemStack stack, int x, int y) {
        int unbreakingLevel2;
        RenderHelper.disableStandardItemLighting();
        int enchantmentY = y + 24;
        if (stack.getItem() instanceof ItemArmor) {
            int protectionLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack);
            int unbreakingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);
            int thornLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack);
            if (protectionLevel > 0) {
                drawEnchantTag("P" + getColor(protectionLevel) + protectionLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (unbreakingLevel > 0) {
                drawEnchantTag("U" + getColor(unbreakingLevel) + unbreakingLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (thornLevel > 0) {
                drawEnchantTag("T" + getColor(thornLevel) + thornLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
        }
        if (stack.getItem() instanceof ItemBow) {
            int powerLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);
            int punchLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, stack);
            int flameLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack);
            unbreakingLevel2 = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);
            if (powerLevel > 0) {
                drawEnchantTag("Pow" + getColor(powerLevel) + powerLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (punchLevel > 0) {
                drawEnchantTag("Pun" + getColor(punchLevel) + punchLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (flameLevel > 0) {
                drawEnchantTag("F" + getColor(flameLevel) + flameLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (unbreakingLevel2 > 0) {
                drawEnchantTag("U" + getColor(unbreakingLevel2) + unbreakingLevel2, x * 2, enchantmentY);
                enchantmentY += 8;
            }
        }
        if (stack.getItem() instanceof ItemSword) {
            int sharpnessLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack);
            int knockbackLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, stack);
            int fireAspectLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack);
            unbreakingLevel2 = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);
            if (sharpnessLevel > 0) {
                drawEnchantTag("S" + getColor(sharpnessLevel) + sharpnessLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (knockbackLevel > 0) {
                drawEnchantTag("K" + getColor(knockbackLevel) + knockbackLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (fireAspectLevel > 0) {
                drawEnchantTag("F" + getColor(fireAspectLevel) + fireAspectLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (unbreakingLevel2 > 0) {
                drawEnchantTag("U" + getColor(unbreakingLevel2) + unbreakingLevel2, x * 2, enchantmentY);
                enchantmentY += 8;
            }
        }
        if (stack.getRarity() == EnumRarity.EPIC) {
            GlStateManager.pushMatrix();
            GlStateManager.disableDepth();
            GL11.glScalef(0.5f, 0.5f, 0.5f);
            drawOutlinedString(Minecraft.getMinecraft().fontRendererObj, "God", x * 2, enchantmentY, new Color(255, 255, 0).getRGB(), new Color(100, 100, 0, 200).getRGB());
            GL11.glScalef(1.0f, 1.0f, 1.0f);
            GlStateManager.enableDepth();
            GlStateManager.popMatrix();
        }
    }

    private static void drawEnchantTag(String text, int x, int y) {
        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GL11.glScalef(0.5f, 0.5f, 0.5f);
        drawOutlinedString(Minecraft.getMinecraft().fontRendererObj, text, x, y, Colors.getColor(255), new Color(0, 0, 0, 100).darker().getRGB());
        GL11.glScalef(1.0f, 1.0f, 1.0f);
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    public static void targetHudRect(double x, double y, double x1, double y1, double size) {
        rectangleBordered(x, y + -4.0, x1 + size, y1 + size, 0.5, new Color(60, 60, 60).getRGB(), new Color(10, 10, 10).getRGB());
        rectangleBordered(x + 1.0, y + -3.0, x1 + size - 1.0, y1 + size - 1.0, 1.0, new Color(40, 40, 40).getRGB(), new Color(40, 40, 40).getRGB());
        rectangleBordered(x + 2.5, y + -1.5, x1 + size - 2.5, y1 + size - 2.5, 0.5, new Color(40, 40, 40).getRGB(), new Color(60, 60, 60).getRGB());
        rectangleBordered(x + 2.5, y + -1.5, x1 + size - 2.5, y1 + size - 2.5, 0.5, new Color(22, 22, 22).getRGB(), new Color(255, 255, 255, 0).getRGB());
    }

    public static void targetHudRect1(double x, double y, double x1, double y1, double size) {
        rectangleBordered(x + 4.35, y + 0.5, x1 + size - 84.5, y1 + size - 4.35, 0.5, new Color(48, 48, 48).getRGB(), new Color(10, 10, 10).getRGB());
        rectangleBordered(x + 5.0, y + 1.0, x1 + size - 85.0, y1 + size - 5.0, 0.5, new Color(17, 17, 17).getRGB(), new Color(255, 255, 255, 0).getRGB());
    }

    public static void drawRectBordered(double x, double y, double x1, double y1, double width, int internalColor, int borderColor) {
        rectangle(x + width, y + width, x1 - width, y1 - width, internalColor);
        rectangle(x + width, y, x1 - width, y + width, borderColor);
        rectangle(x, y, x + width, y1, borderColor);
        rectangle(x1 - width, y, x1, y1, borderColor);
        rectangle(x + width, y1 - width, x1 - width, y1, borderColor);
    }

    public static void enableSmoothLine(float width) {
        GL11.glDisable(3008);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glEnable(2884);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
        GL11.glLineWidth(width);
    }

    public static void disableSmoothLine() {
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDisable(3042);
        GL11.glEnable(3008);
        GL11.glDepthMask(true);
        GL11.glCullFace(1029);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
    }

    public static void drawNewRect(float left, float top, float right, float bottom, int color) {
        if (left < right) {
            float i = left;
            left = right;
            right = i;
        }
        if (top < bottom) {
            float j = top;
            top = bottom;
            bottom = j;
        }
        float f3 = (float)(color >> 24 & 0xFF) / 255.0f;
        float f = (float)(color >> 16 & 0xFF) / 255.0f;
        float f1 = (float)(color >> 8 & 0xFF) / 255.0f;
        float f2 = (float)(color & 0xFF) / 255.0f;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, bottom, 0.0).endVertex();
        worldrenderer.pos(right, bottom, 0.0).endVertex();
        worldrenderer.pos(right, top, 0.0).endVertex();
        worldrenderer.pos(left, top, 0.0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void renderItemStack(ItemStack stack, int x, int y) {
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.clear(256);
        RenderHelper.enableStandardItemLighting();
        Minecraft.getMinecraft().getRenderItem().zLevel = -150.0f;
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
        Minecraft.getMinecraft().getRenderItem().renderItemOverlays(Minecraft.getMinecraft().fontRendererObj, stack, x, y);
        Minecraft.getMinecraft().getRenderItem().zLevel = 0.0f;
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableCull();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    public static void prepareScissorBox(float x, float y, float x2, float y2) {
        ScaledResolution scale = new ScaledResolution(Minecraft.getMinecraft());
        int factor = scale.getScaleFactor();
        GL11.glScissor((int)(x * (float)factor), (int)(((float)scale.getScaledHeight() - y2) * (float)factor), (int)((x2 - x) * (float)factor), (int)((y2 - y) * (float)factor));
    }
}
