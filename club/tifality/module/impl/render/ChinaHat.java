package club.tifality.module.impl.render;

import club.tifality.manager.api.annotations.Listener;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.module.ModuleManager;
import club.tifality.module.impl.combat.KillAura;
import club.tifality.module.impl.other.SilentView;
import club.tifality.module.impl.player.Scaffold;
import club.tifality.property.Property;
import club.tifality.property.impl.EnumProperty;
import club.tifality.utils.MathUtils;
import club.tifality.utils.render.Colors;
import club.tifality.utils.render.ESPUtil;
import club.tifality.utils.render.RenderingUtils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import club.tifality.manager.event.impl.player.UpdatePositionEvent;
import club.tifality.manager.event.impl.render.Render3DEvent;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_FLAT;

@ModuleInfo(label = "ChinaHat", category = ModuleCategory.RENDER)
public final class ChinaHat extends Module {
    private final Property<Boolean> firstPerson = new Property<>("Show in first person", false);
    private final Property<Boolean> allPlayers = new Property<>("All players", false);
    private final EnumProperty<Mode> colorMode = new EnumProperty<>("Color Mode", Mode.Rainbow);
    private final Property<Integer> color = new Property<>("Color", Colors.WHITE, () -> colorMode.get() == Mode.Custom);
    private final Property<Integer> fadeIn = new Property<>("Fade In", Colors.WHITE, () -> colorMode.get() == Mode.Fade);
    private final Property<Integer> fadeOut = new Property<>("Fade Out", new Color(Colors.WHITE).darker().darker().getRGB(), () -> colorMode.get() == Mode.Fade);

    @Listener
    public void onRender3D(Render3DEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        float partialTicks = event.getPartialTicks();

        double renderPosX = RenderManager.renderPosX, renderPosY = RenderManager.renderPosY, renderPosZ = RenderManager.renderPosZ;

        glShadeModel(GL_SMOOTH);
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.disableCull();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            boolean self = player == mc.thePlayer;
            if ((!allPlayers.get() && !self) || (self && !firstPerson.get() && mc.gameSettings.thirdPersonView == 0)
                    || player.isDead || player.isInvisible() || (!self && (!ESPUtil.isInView(player) || !mc.thePlayer.canEntityBeSeen(player))))
                continue;

            glPushMatrix();

            double posX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks - renderPosX,
                    posY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks - renderPosY,
                    posZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks - renderPosZ;

            AxisAlignedBB bb = player.getEntityBoundingBox();

            //boolean lowerHeight = CustomModel.enabled && mc.gameSettings.thirdPersonView != 0;
            //double height = (lowerHeight ? -CustomModel.getYOffset() : 0) + bb.maxY - bb.minY + 0.02, radius = bb.maxX - bb.minX;
            double height = bb.maxY - bb.minY + 0.02, radius = bb.maxX - bb.minX;

            float yaw;
            float pitch;
            KillAura killAura = ModuleManager.getInstance(KillAura.class);
            Scaffold scaffold = ModuleManager.getInstance(Scaffold.class);
            SilentView silentView = ModuleManager.getInstance(SilentView.class);
            EntityPlayerSP playerSP = (EntityPlayerSP) player;
            final UpdatePositionEvent updateEvent = playerSP.currentEvent;
            if ((killAura.isEnabled() || scaffold.isEnabled()) && silentView.isEnabled()) {
                yaw = MathUtils.interpolate(MathHelper.wrapAngleTo180_float(updateEvent.getPrevYaw()),
                        MathHelper.wrapAngleTo180_float(updateEvent.getYaw()), partialTicks).floatValue();
                pitch = MathUtils.interpolate(MathHelper.wrapAngleTo180_float(updateEvent.getPrevPitch()),
                        MathHelper.wrapAngleTo180_float(updateEvent.getPitch()), partialTicks).floatValue();
            } else {
                yaw = MathUtils.interpolate(player.prevRotationYawHead, player.rotationYawHead, partialTicks).floatValue();
                pitch = MathUtils.interpolate(player.prevRotationPitch, player.rotationPitch, partialTicks).floatValue();
            }

            glTranslated(0, posY + height, 0);

            glEnable(GL_LINE_SMOOTH);
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
            if (self) glRotated(yaw, 0, -1, 0);
            glRotated(pitch / 3.0, 1, 0, 0);
            glTranslated(0, 0, pitch / 270.0);
            glLineWidth(2);
            glBegin(GL_LINE_LOOP);

            int color = 0;
            // outline/border or whatever you call it
            for (int i = 0; i <= 180; i++) {
                switch (colorMode.get()) {
                    case Rainbow:
                        color = Colors.rainbow(7, i * 4, 0.5F, 1, .5f).getRGB();
                        break;
                    case Fade:
                        color = Colors.applyOpacity(new Color(Colors.interpolateColorsBackAndForth(7, i * 4, new Color(fadeIn.get()), new Color(fadeOut.get()), false).getRGB()).getRGB(), .5f);
                        break;
                    case Custom:
                        color = this.color.get();
                        break;
                }
                RenderingUtils.color(color);
                glVertex3d(
                        posX - Math.sin(i * MathHelper.PI2 / 90) * radius,
                        -(player.isSneaking() ? 0.23 : 0) - 0.002,
                        posZ + Math.cos(i * MathHelper.PI2 / 90) * radius
                );
            }
            glEnd();

            glBegin(GL_TRIANGLE_FAN);
            switch (colorMode.get()) {
                case Rainbow:
                    color = Colors.rainbow(7, 4, 0.5F, 1, .5f).getRGB();
                    break;
                case Fade:
                    color = Colors.applyOpacity(new Color(Colors.interpolateColorsBackAndForth(7, 4, new Color(fadeIn.get()), new Color(fadeOut.get()), false).getRGB()).getRGB(), .5f);
                    break;
                case Custom:
                    color = this.color.get();
                    break;
            }
            RenderingUtils.color(color);
            glVertex3d(posX, 0.3 - (player.isSneaking() ? 0.23 : 0), posZ);

            // draw hat
            for (int i = 0; i <= 180; i++) {
                switch (colorMode.get()) {
                    case Rainbow:
                        color = Colors.rainbow(7, i * 4, 0.5F, 1, .5f).getRGB();
                        break;
                    case Fade:
                        color = Colors.applyOpacity(new Color(Colors.interpolateColorsBackAndForth(7, i * 4, new Color(fadeIn.get()), new Color(fadeOut.get()), false).getRGB()).getRGB(), .5f);
                        break;
                    case Custom:
                        color = this.color.get();
                        break;
                }
                RenderingUtils.color(color);
                glVertex3d(posX - Math.sin(i * MathHelper.PI2 / 90) * radius,
                        -(player.isSneaking() ? 0.23F : 0),
                        posZ + Math.cos(i * MathHelper.PI2 / 90) * radius
                );

            }
            glVertex3d(posX, 0.3 - (player.isSneaking() ? 0.23 : 0), posZ);
            glEnd();
            glPopMatrix();
        }

        RenderingUtils.resetColor();
        GlStateManager.enableCull();
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        glDisable(GL_LINE_SMOOTH);
        glShadeModel(GL_FLAT);
    }

    private enum Mode {
        Rainbow,
        Fade,
        Custom;
    }
}
