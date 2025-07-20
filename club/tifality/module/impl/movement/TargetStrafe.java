package club.tifality.module.impl.movement;

import club.tifality.Tifality;
import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.event.impl.player.MoveEntityEvent;
import club.tifality.manager.event.impl.render.overlay.Render2DEvent;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.module.ModuleManager;
import club.tifality.utils.RotationUtils;
import club.tifality.utils.Wrapper;
import club.tifality.utils.movement.MovementUtils;
import club.tifality.utils.render.OGLUtils;
import club.tifality.utils.render.RenderingUtils;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;
import club.tifality.manager.event.impl.player.UpdatePositionEvent;
import club.tifality.manager.event.impl.render.Render3DEvent;
import club.tifality.module.Module;
import club.tifality.module.impl.combat.KillAura;
import club.tifality.property.Property;
import club.tifality.property.impl.DoubleProperty;
import club.tifality.property.impl.EnumProperty;
import club.tifality.property.impl.Representation;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@ModuleInfo(label = "TargetStrafe", category = ModuleCategory.MOVEMENT)
public class TargetStrafe extends Module {
    public final DoubleProperty radiusProperty = new DoubleProperty("Radius", 2.0, 0.1, 4.0, 0.1, Representation.DISTANCE);
    public final Property<Boolean> holdSpaceProperty = new Property<>("Hold Space", true);
    public final Property<Boolean> behindProperty = new Property<>("Behind", true);
    public final Property<Boolean> keepRangeValue = new Property<>("Keep Range", false, () -> !this.behindProperty.get());
    public final Property<Boolean> adaptiveSpeedProperty = new Property<>("Adaptive", true);
    public final EnumProperty<RenderMode> renderModeValue = new EnumProperty<>("Mode", RenderMode.NORMAL);
    private final Property<Integer> renderColorValue = new Property<>("Color", new Color(120, 255, 120).getRGB(), () -> this.renderModeValue.get() == RenderMode.NORMAL);
    public final DoubleProperty shapeValue = new DoubleProperty("Shape", 12.0, () -> this.renderModeValue.get() == RenderMode.NORMAL, 0.0, 30.0, 1.0);
    private final DoubleProperty pointsProperty = new DoubleProperty("Points", 12.0, () -> this.renderModeValue.get() == RenderMode.POINT, 1.0, 90.0, 1.0);
    private final Property<Integer> activePointColorProperty = new Property<>("Active Color", -2147418368, () -> this.renderModeValue.get() == RenderMode.POINT);
    private final Property<Integer> dormantPointColorProperty = new Property<>("Dormant Color", 553648127, () -> this.renderModeValue.get() == RenderMode.POINT);
    private final Property<Integer> invalidPointColorProperty = new Property<>("Invalid Color", 553582592, () -> this.renderModeValue.get() == RenderMode.POINT);
    private final List<Point3D> currentPoints = new ArrayList<>();
    private Point3D currentPoint;
    private EntityLivingBase currentTarget;
    int consts;
    double lastDist;

    /*public TargetStrafe() {
        this.setSuffix(() -> this.adaptiveSpeedProperty.get() ? "Adaptive" : "Normal");
    }*/

    @Listener
    public void onRenderSuffix(Render2DEvent event) {
        String suffix = this.adaptiveSpeedProperty.get() ? "Adaptive" : "Normal";
        this.setUpdatedSuffix(suffix);
    }

    @Listener
    public void onUpdatePositionEvent(UpdatePositionEvent event) {
        if (event.isPre()) {
            EntityLivingBase target = KillAura.getInstance().getTarget();
            if (target != null) {
                this.collectPoints(this.currentTarget = target);
                this.currentPoint = this.findOptimalPoint(target, this.currentPoints);
            } else {
                this.currentTarget = null;
                this.currentPoint = null;
            }
        }
    }

    @Listener
    public void onRender3DEvent(Render3DEvent event) {
        Speed speed = Tifality.getInstance().getModuleManager().getModule(Speed.class);
        Flight fly = Tifality.getInstance().getModuleManager().getModule(Flight.class);
        KillAura killAura = ModuleManager.getInstance(KillAura.class);
        if (this.renderModeValue.get() == RenderMode.POINT && killAura.getTarget() != null) {
            float partialTicks = event.getPartialTicks();
            for (Point3D point : this.currentPoints) {
                int color;
                if (this.currentPoint == point) {
                    color = this.activePointColorProperty.get();
                } else if (point.valid) {
                    color = this.dormantPointColorProperty.get();
                } else {
                    color = this.invalidPointColorProperty.get();
                }
                double x = RenderingUtils.interpolate(point.prevX, point.x, partialTicks);
                double y = RenderingUtils.interpolate(point.prevY, point.y, partialTicks);
                double z = RenderingUtils.interpolate(point.prevZ, point.z, partialTicks);
                double pointSize = 0.03;
                AxisAlignedBB bb = new AxisAlignedBB(x, y, z, x + pointSize, y + pointSize, z + pointSize);
                OGLUtils.enableBlending();
                OGLUtils.disableDepth();
                OGLUtils.disableTexture2D();
                OGLUtils.color(color);
                double renderX = RenderManager.renderPosX;
                double renderY = RenderManager.renderPosY;
                double renderZ = RenderManager.renderPosZ;
                GL11.glTranslated(-renderX, -renderY, -renderZ);
                RenderGlobal.func_181561_a(bb, false, true);
                GL11.glTranslated(renderX, renderY, renderZ);
                OGLUtils.disableBlending();
                OGLUtils.enableDepth();
                OGLUtils.enableTexture2D();
            }
        }
        if (this.renderModeValue.get() == RenderMode.NORMAL) {
            double x2 = killAura.getTarget().lastTickPosX + (killAura.getTarget().posX - killAura.getTarget().lastTickPosX) * mc.timer.renderPartialTicks - RenderManager.viewerPosX;
            double y2 = killAura.getTarget().lastTickPosY + (killAura.getTarget().posY - killAura.getTarget().lastTickPosY) * mc.timer.renderPartialTicks - RenderManager.viewerPosY;
            double z2 = killAura.getTarget().lastTickPosZ + (killAura.getTarget().posZ - killAura.getTarget().lastTickPosZ) * mc.timer.renderPartialTicks - RenderManager.viewerPosZ;
            RenderingUtils.TScylinder2(killAura.getTarget(), x2, y2, z2, this.radiusProperty.get() - 0.00625, 6.0f, this.shapeValue.get().intValue(), new Color(0, 0, 0).getRGB());
            RenderingUtils.TScylinder2(killAura.getTarget(), x2, y2, z2, this.radiusProperty.get() + 0.00625, 6.0f, this.shapeValue.get().intValue(), new Color(0, 0, 0).getRGB());
            if (speed.isEnabled() || fly.isEnabled()) {
                RenderingUtils.TScylinder1(killAura.getTarget(), x2, y2, z2, this.radiusProperty.get(), this.shapeValue.get().intValue(), 5.0f, this.renderColorValue.get());
            } else {
                RenderingUtils.TScylinder1(killAura.getTarget(), x2, y2, z2, this.radiusProperty.get(), this.shapeValue.get().intValue(), 5.0f, new Color(255, 255, 255).getRGB());
            }
        }
    }

    public boolean shouldAdaptSpeed() {
        if (!this.adaptiveSpeedProperty.get()) {
            return false;
        }
        EntityPlayerSP player = Wrapper.getPlayer();
        double xDist = this.currentPoint.x - player.posX;
        double zDist = this.currentPoint.z - player.posZ;
        return StrictMath.sqrt(xDist * xDist + zDist * zDist) < 0.2;
    }

    public double getAdaptedSpeed() {
        if (this.currentTarget == null) {
            return 0.0;
        }
        double xDist = this.currentTarget.posX - this.currentTarget.prevPosX;
        double zDist = this.currentTarget.posZ - this.currentTarget.prevPosZ;
        return StrictMath.sqrt(xDist * xDist + zDist * zDist);
    }

    public static TargetStrafe getInstance() {
        return ModuleManager.getInstance(TargetStrafe.class);
    }

    public boolean shouldStrafe() {
        return this.currentPoint != null;
    }

    public void setSpeed(MoveEntityEvent event, double speed) {
        MovementUtils.setSpeed(event, speed, 1.0f, 0.0f, this.getYawToPoint(this.currentPoint));
    }

    private float getYawToPoint(Point3D point) {
        EntityPlayerSP player = Wrapper.getPlayer();
        double xDist = point.x - player.posX;
        double zDist = point.z - player.posZ;
        float rotationYaw = player.rotationYaw;
        float angle = (float)(StrictMath.atan2(zDist, xDist) * 180.0 / Math.PI) - 90.0f;
        return rotationYaw + MathHelper.wrapAngleTo180_float(angle - rotationYaw);
    }

    private Point3D findOptimalPoint(EntityLivingBase target, List<Point3D> points) {
        if (this.behindProperty.get()) {
            Point3D bestPoint = null;
            float biggestDif = -1.0f;
            for (Point3D point : points) {
                if (point.valid) {
                    float yawChange = Math.abs(this.getYawChangeToPoint(target, point));
                    if (yawChange <= biggestDif) {
                        continue;
                    }
                    biggestDif = yawChange;
                    bestPoint = point;
                }
            }
            return bestPoint;
        }
        return null;
    }

    private float getYawChangeToPoint(EntityLivingBase target, Point3D point) {
        double xDist = point.x - target.posX;
        double zDist = point.z - target.posZ;
        float rotationYaw = target.rotationYaw;
        float angle = (float)(StrictMath.atan2(zDist, xDist) * 180.0 / Math.PI) - 90.0f;
        return rotationYaw + MathHelper.wrapAngleTo180_float(angle - rotationYaw);
    }

    private void collectPoints(EntityLivingBase entity) {
        int size = this.pointsProperty.get().intValue();
        double radius = this.radiusProperty.get();
        this.currentPoints.clear();
        double x = entity.posX;
        double y = entity.posY;
        double z = entity.posZ;
        double prevX = entity.prevPosX;
        double prevY = entity.prevPosY;
        double prevZ = entity.prevPosZ;
        for (int i = 0; i < size; ++i) {
            double cos = radius * StrictMath.cos(i * 6.2831855f / size);
            double sin = radius * StrictMath.sin(i * 6.2831855f / size);
            double pointX = x + cos;
            double pointZ = z + sin;
            Point3D point = new Point3D(pointX, y, pointZ, prevX + cos, prevY, prevZ + sin, this.validatePoint(pointX, pointZ));
            this.currentPoints.add(point);
        }
    }

    private boolean validatePoint(double x, double z) {
        Vec3 pointVec = new Vec3(x, Wrapper.getPlayer().posY, z);
        IBlockState blockState = Wrapper.getWorld().getBlockState(new BlockPos(pointVec));
        boolean canBeSeen = Wrapper.getWorld().rayTraceBlocks(Wrapper.getPlayer().getPositionVector(), pointVec, false, false, false) == null;
        return !this.isOverVoid(x, z) && !blockState.getBlock().canCollideCheck(blockState, false) && canBeSeen;
    }

    private boolean isOverVoid(double x, double z) {
        for (double posY = Wrapper.getPlayer().posY; posY > 0.0; --posY) {
            if (!(Wrapper.getWorld().getBlockState(new BlockPos(x, posY, z)).getBlock() instanceof BlockAir)) {
                return false;
            }
        }
        return true;
    }

    @Listener
    public void moveStrafe(final MoveEntityEvent event) {
        EntityLivingBase target = KillAura.getInstance().getTarget();
        double xDist = event.getX();
        double zDist = event.getZ();
        this.lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
        if (this.behindProperty.get()) {
            if (this.keyMode() && target != null && this.shouldStrafe()) {
                if (this.shouldAdaptSpeed()) {
                    this.lastDist = Math.min(this.lastDist, this.getAdaptedSpeed());
                }
                if (this.canStrafe()) {
                    this.setSpeed(event, this.lastDist);
                }
                return;
            }
            if (this.canStrafe()) {
                MovementUtils.setSpeed(event, this.lastDist, mc.thePlayer.moveForward, mc.thePlayer.moveStrafing, mc.thePlayer.rotationYaw);
            }
        } else if (!this.isVoid(0, 0) && this.canStrafe()) {
            float rotations = RotationUtils.getYawToEntity(target, false);
            this.setSpeed(event, this.lastDist, rotations, this.radiusProperty.get(), 1.0);
        }
    }

    public boolean keyMode() {
        boolean strafe = mc.thePlayer.movementInput.moveStrafe != 0.0f || mc.thePlayer.movementInput.moveForward != 0.0f;
        if (this.holdSpaceProperty.get()) {
            return mc.gameSettings.keyBindJump.isKeyDown() && strafe;
        }
        return strafe;
    }

    public boolean canStrafe() {
        Speed speed = Tifality.getInstance().getModuleManager().getModule(Speed.class);
        Flight fly = Tifality.getInstance().getModuleManager().getModule(Flight.class);
        return (speed.isEnabled() || fly.isEnabled()) && KillAura.getInstance().getTarget() != null && !mc.thePlayer.isSneaking() && this.keyMode();
    }

    public Float canSize() {
        return this.adaptiveSpeedProperty.get() ? 30.0f : (45.0f / (float)this.getEnemyDistance());
    }

    private float getAlgorithm() {
        return (float)Math.max(this.getEnemyDistance() - this.radiusProperty.get(), this.getEnemyDistance() - this.getEnemyDistance() - this.radiusProperty.get() / this.radiusProperty.get() * 2.0);
    }

    private double getEnemyDistance() {
        return mc.thePlayer.getDistance(KillAura.getInstance().getTarget().posX, mc.thePlayer.posY, KillAura.getInstance().getTarget().posZ) + 0.4000000059604645 - 0.1;
    }

    public void setSpeed(MoveEntityEvent moveEvent, double moveSpeed, float pseudoYaw, double pseudoStrafe, double pseudoForward) {
        float yaw = pseudoYaw;
        float strafe2 = 0.0f;
        double forward = pseudoForward;
        double strafe3 = pseudoStrafe;
        if (mc.thePlayer.isCollidedHorizontally || this.checkVoid()) {
            if (this.consts < 2) {
                ++this.consts;
            } else {
                this.consts = -1;
            }
        }
        switch (this.consts) {
            case 0: {
                this.consts = 1;
                break;
            }
            case 2: {
                this.consts = -1;
                break;
            }
        }
        if (this.holdSpaceProperty.get()) {
            strafe3 = pseudoStrafe * 0.98 * this.consts;
        } else {
            strafe3 = this.consts;
        }
        if (forward != 0.0) {
            if (strafe3 > 0.0) {
                if (this.keepRangeValue.get() && this.getEnemyDistance() < this.radiusProperty.get() && !mc.thePlayer.isCollidedHorizontally && !this.checkVoid()) {
                    yaw += ((forward > 0.0) ? (-this.canSize()) : this.canSize());
                }
                strafe2 += ((forward > 0.0) ? (-60.0f / this.getAlgorithm()) : (60.0f / this.getAlgorithm()));
            } else if (strafe3 < 0.0) {
                if (this.keepRangeValue.get() && this.getEnemyDistance() < this.radiusProperty.get() && !mc.thePlayer.isCollidedHorizontally && !this.checkVoid()) {
                    yaw += ((forward > 0.0) ? (-this.canSize()) : this.canSize());
                }
                strafe2 += ((forward > 0.0) ? (60.0f / this.getAlgorithm()) : (-60.0f / this.getAlgorithm()));
            }
            strafe3 = 0.0;
            if (forward > 0.0) {
                forward = 1.0;
            } else if (forward < 0.0) {
                forward = -1.0;
            }
        }
        if (strafe3 > 0.0) {
            strafe3 = 1.0;
        } else if (strafe3 < 0.0) {
            strafe3 = -1.0;
        }
        double mx = Math.cos(Math.toRadians(yaw + 90.0f + strafe2));
        double mz = Math.sin(Math.toRadians(yaw + 90.0f + strafe2));
        moveEvent.setX(forward * moveSpeed * mx + strafe3 * moveSpeed * mz);
        moveEvent.setZ(forward * moveSpeed * mz - strafe3 * moveSpeed * mx);
    }

    private boolean checkVoid() {
        for (int x = -1; x < 1; ++x) {
            for (int z = -1; z < 1; ++z) {
                if (this.isVoid(x, z)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isVoid(int X, int Z) {
        Flight fly = Tifality.getInstance().getModuleManager().getModule(Flight.class);
        if (fly.isEnabled()) {
            return false;
        }
        if (mc.thePlayer.posY < 0.0) {
            return true;
        }
        for (int off = 0; off < (int)mc.thePlayer.posY + 2; off += 2) {
            final AxisAlignedBB bb = mc.thePlayer.getEntityBoundingBox().offset(X, -off, Z);
            if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static class Point3D {
        private final double x;
        private final double y;
        private final double z;
        private final double prevX;
        private final double prevY;
        private final double prevZ;
        private final boolean valid;

        public Point3D(double x, double y, double z, double prevX, double prevY, double prevZ, boolean valid) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.prevX = prevX;
            this.prevY = prevY;
            this.prevZ = prevZ;
            this.valid = valid;
        }
    }

    public enum RenderMode {
        NORMAL,
        POINT,
        OFF;
    }
}
