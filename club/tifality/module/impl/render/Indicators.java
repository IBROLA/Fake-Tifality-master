package club.tifality.module.impl.render;

import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.event.impl.render.Render3DEvent;
import club.tifality.manager.event.impl.render.overlay.Render2DEvent;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.property.Property;
import club.tifality.property.impl.DoubleProperty;
import club.tifality.utils.render.RenderingUtils;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@ModuleInfo(label = "Indicators", category = ModuleCategory.RENDER)
public class Indicators extends Module {
    private final DoubleProperty size = new DoubleProperty("Size", 10.0, 5.0, 25.0, 1.0);
    private final DoubleProperty radius = new DoubleProperty("Radius", 45.0, 10.0, 200.0, 1.0);
    private final Property<Boolean> fade = new Property<>("Fade", true);
    private int alpha;
    private boolean plus_or_minus;
    private final EntityListener entityListener = new EntityListener();

    @Override
    public void onEnable() {
        this.alpha = 0;
        this.plus_or_minus = false;
    }

    @Listener
    public void onRender3D(Render3DEvent event) {
        this.entityListener.render3d(event);
    }

    @Listener
    public void onRender2D(Render2DEvent event) {
        if (this.fade.get()) {
            float speed = 0.0025f;
            if (this.alpha <= 60.0f || this.alpha >= 255.0f) {
                this.plus_or_minus = !this.plus_or_minus;
            }
            if (this.plus_or_minus) {
                this.alpha += (int)speed;
            } else {
                this.alpha -= (int)speed;
            }
            this.alpha = (int)clamp(this.alpha, 60.0, 255.0);
        } else {
            this.alpha = 255;
        }
        mc.theWorld.loadedEntityList.forEach(o -> {
            if (o instanceof EntityPlayer) {
                EntityPlayer entity = (EntityPlayer)o;
                Vec3 pos = this.entityListener.getEntityLowerBounds().get(entity);
                if (pos != null && !this.isOnScreen(pos)) {
                    int x = Display.getWidth() / 2 / (Indicators.mc.gameSettings.guiScale == 0 ? 1 : Indicators.mc.gameSettings.guiScale);
                    int y = Display.getHeight() / 2 / (Indicators.mc.gameSettings.guiScale == 0 ? 1 : Indicators.mc.gameSettings.guiScale);
                    float yaw = this.getRotations(entity) - Indicators.mc.thePlayer.rotationYaw;
                    GL11.glTranslatef(x, y, 0.0f);
                    GL11.glRotatef(yaw, 0.0f, 0.0f, 1.0f);
                    GL11.glTranslatef(-x, -y, 0.0f);
                    RenderingUtils.drawTracerPointer(x, (float)y - this.radius.getValue().floatValue(), this.size.getValue().floatValue(), 2.0f, 1.0f, this.getColor(entity, this.alpha).getRGB());
                    GL11.glTranslatef(x, y, 0.0f);
                    GL11.glRotatef(-yaw, 0.0f, 0.0f, 1.0f);
                    GL11.glTranslatef(-x, -y, 0.0f);
                }
            }
        });
    }

    private boolean isOnScreen(Vec3 pos) {
        return pos.xCoord > -1.0 && pos.zCoord < 1.0 && pos.xCoord / ((Indicators.mc.gameSettings.guiScale == 0) ? 1 : Indicators.mc.gameSettings.guiScale) >= 0.0 && pos.xCoord / ((Indicators.mc.gameSettings.guiScale == 0) ? 1 : Indicators.mc.gameSettings.guiScale) <= Display.getWidth() && pos.yCoord / ((Indicators.mc.gameSettings.guiScale == 0) ? 1 : Indicators.mc.gameSettings.guiScale) >= 0.0 && pos.yCoord / ((Indicators.mc.gameSettings.guiScale == 0) ? 1 : Indicators.mc.gameSettings.guiScale) <= Display.getHeight();
    }

    private float getRotations(EntityLivingBase ent) {
        final double x = ent.posX - Indicators.mc.thePlayer.posX;
        final double z = ent.posZ - Indicators.mc.thePlayer.posZ;
        return (float)(-(Math.atan2(x, z) * 57.29577951308232));
    }

    private Color getColor(EntityLivingBase player, int alpha) {
        final float f = Indicators.mc.thePlayer.getDistanceToEntity(player);
        final float f2 = 40.0f;
        final float f3 = Math.max(0.0f, Math.min(f, f2) / f2);
        final Color clr = new Color(Color.HSBtoRGB(f3 / 3.0f, 1.0f, 1.0f) | 0xFF000000);
        return new Color(clr.getRed(), clr.getGreen(), clr.getBlue(), alpha);
    }

    public static double clamp(double value, double minimum, double maximum) {
        return (value > maximum) ? maximum : Math.max(value, minimum);
    }

    public static class EntityListener {
        private final Map<Entity, Vec3> entityUpperBounds = new HashMap<>();
        private final Map<Entity, Vec3> entityLowerBounds = new HashMap<>();

        private void render3d(Render3DEvent event) {
            if (!this.entityUpperBounds.isEmpty()) {
                this.entityUpperBounds.clear();
            }
            if (!this.entityLowerBounds.isEmpty()) {
                this.entityLowerBounds.clear();
            }
            for (Entity e : Module.mc.theWorld.loadedEntityList) {
                final Vec3 bound = this.getEntityRenderPosition(e);
                bound.add(new Vec3(0.0, e.height + 0.2, 0.0));
                final Vec3 upperBounds = RenderingUtils.to2D(bound.xCoord, bound.yCoord, bound.zCoord);
                final Vec3 lowerBounds = RenderingUtils.to2D(bound.xCoord, bound.yCoord - 2.0, bound.zCoord);
                if (upperBounds != null && lowerBounds != null) {
                    this.entityUpperBounds.put(e, upperBounds);
                    this.entityLowerBounds.put(e, lowerBounds);
                }
            }
        }

        private Vec3 getEntityRenderPosition(Entity entity) {
            final double partial = Module.mc.timer.renderPartialTicks;
            final double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partial - RenderManager.viewerPosX;
            final double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partial - RenderManager.viewerPosY;
            final double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partial - RenderManager.viewerPosZ;
            return new Vec3(x, y, z);
        }

        public Map<Entity, Vec3> getEntityLowerBounds() {
            return this.entityLowerBounds;
        }
    }
}
