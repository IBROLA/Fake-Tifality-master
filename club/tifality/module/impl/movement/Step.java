package club.tifality.module.impl.movement;

import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.event.impl.player.MoveEntityEvent;
import club.tifality.manager.event.impl.player.StepConfirmEvent;
import club.tifality.manager.event.impl.player.StepEvent;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.stats.StatList;
import club.tifality.manager.event.impl.player.UpdatePositionEvent;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.module.ModuleManager;
import club.tifality.property.impl.DoubleProperty;
import club.tifality.property.impl.EnumProperty;
import club.tifality.property.impl.Representation;
import club.tifality.utils.timer.TimerUtil;
import club.tifality.utils.movement.MovementUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

@ModuleInfo(label = "Step", category = ModuleCategory.MOVEMENT)
public final class Step extends Module {
    private final EnumProperty<StepMode> modeValue = new EnumProperty<>("Mode", StepMode.NCP);
    private final DoubleProperty timerValue = new DoubleProperty("Timer", 0.5, 0.1, 1.0, 0.1);
    private final DoubleProperty heightValue = new DoubleProperty("Height", 1.0, 0.6, 10.0, 0.1, Representation.DISTANCE);
    private final DoubleProperty delayValue = new DoubleProperty("Delay", 0.0, 0.0, 500.0, 1.0, Representation.MILLISECONDS);
    private boolean isStep;
    private double stepX;
    private double stepY;
    private double stepZ;
    private int ncpNextStep;
    private final TimerUtil timer = new TimerUtil();
    public boolean cancelStep = false;

    public DoubleProperty getHeightValue() {
        return this.heightValue;
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer != null) {
            mc.thePlayer.stepHeight = 0.5f;
        }
    }

    @Listener
    public void onUpdate(UpdatePositionEvent event) {
        if (this.stepY == new BigDecimal(this.stepY).setScale(3, RoundingMode.HALF_DOWN).doubleValue()) {
            mc.timer.timerSpeed = 1.0f;
        }
    }

    @Listener
    public void onMove(MoveEntityEvent event) {
        if (mc.thePlayer != null) {
            if (this.modeValue.get() == StepMode.MOTIONNCP && mc.thePlayer.isCollidedHorizontally) {
                if (!mc.gameSettings.keyBindJump.isKeyDown()) {
                    if (mc.thePlayer.onGround && this.couldStep()) {
                        this.fakeJump();
                        mc.thePlayer.motionY = 0.0;
                        event.setY(0.41999998688698);
                        this.ncpNextStep = 1;
                    } else if (this.ncpNextStep == 1) {
                        event.setY(0.33319999363422);
                        this.ncpNextStep = 2;
                    } else if (this.ncpNextStep == 2) {
                        double yaw = MovementUtils.getDirection();
                        event.setY(0.24813599859094704);
                        event.setX(-Math.sin(yaw) * 0.7);
                        event.setZ(Math.cos(yaw) * 0.7);
                        this.ncpNextStep = 0;
                    }
                }
            }
        }
    }

    @Listener
    public void onStep(StepEvent event) {
        if (mc.thePlayer == null) {
            return;
        }
        event.setStepHeight(this.cancelStep ? 0.0f : 1.0f);
        Phase phase = ModuleManager.getInstance(Phase.class);
        if (phase.isEnabled()) {
            event.setStepHeight(0.0f);
            return;
        }
        Speed speed = ModuleManager.getInstance(Speed.class);
        if (speed == null) {
            throw new NullPointerException("null cannot be cast to non-null type vip.radium.module.impl.movement.Speed");
        }
        if (speed.isEnabled()) {
            if (speed.getStepCheck().get()) {
                event.setStepHeight(0.0f);
                return;
            }
        }
        if (!mc.thePlayer.onGround || !this.timer.hasElapsed(speed.isEnabled() ? 100L : ((long)(this.delayValue).get().doubleValue())) || this.modeValue.get() == StepMode.MOTIONNCP) {
            event.setStepHeight(mc.thePlayer.stepHeight = 0.5f);
            return;
        }
        mc.thePlayer.stepHeight = (float)this.heightValue.get().doubleValue();
        event.setStepHeight((float)this.heightValue.get().doubleValue());
        if (event.getStepHeight() > 0.5f) {
            this.isStep = true;
            this.stepX = mc.thePlayer.posX;
            this.stepY = mc.thePlayer.posY;
            this.stepZ = mc.thePlayer.posZ;
        }
    }

    @Listener
    public void onStepConfirm(StepConfirmEvent event) {
        if (mc.thePlayer == null || !this.isStep) {
            return;
        }
        if (mc.thePlayer.getEntityBoundingBox().minY - this.stepY > 0.625 && this.modeValue.get() == StepMode.NCP) {
            this.fakeJump();
            mc.timer.timerSpeed = (float)(this.timerValue).get().doubleValue();
            mc.getNetHandler().sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(this.stepX, this.stepY + 0.41999998688698, this.stepZ, false));
            mc.getNetHandler().sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(this.stepX, this.stepY + 0.7531999805212, this.stepZ, false));
            this.timer.reset();
        }
        this.isStep = false;
        this.stepX = 0.0;
        this.stepY = 0.0;
        this.stepZ = 0.0;
    }

    private void fakeJump() {
        if (mc.thePlayer != null) {
            mc.thePlayer.isAirBorne = true;
            mc.thePlayer.triggerAchievement(StatList.jumpStat);
        }
    }

    private boolean couldStep() {
        double yaw = MovementUtils.getDirection();
        double x = -Math.sin(yaw) * 0.4;
        double z = Math.cos(yaw) * 0.4;
        return mc.theWorld.getCollisionBoxes(mc.thePlayer.getEntityBoundingBox().offset(x, 1.001335979112147, z)).isEmpty();
    }

    public boolean getOnStep() {
        return this.isEnabled() && this.isStep;
    }

    private enum StepMode {
        VANILLA,
        NCP,
        MOTIONNCP;
    }
}
