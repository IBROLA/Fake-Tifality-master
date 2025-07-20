package club.tifality.module.impl.movement;

import club.tifality.Tifality;
import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.api.annotations.Priority;
import club.tifality.manager.event.impl.packet.PacketReceiveEvent;
import club.tifality.manager.event.impl.player.MoveEntityEvent;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.module.ModuleManager;
import club.tifality.module.impl.other.GameSpeed;
import club.tifality.module.impl.player.Scaffold;
import club.tifality.property.Property;
import club.tifality.property.impl.DoubleProperty;
import club.tifality.property.impl.EnumProperty;
import club.tifality.utils.MathUtils;
import club.tifality.utils.Wrapper;
import club.tifality.utils.movement.MovementUtils;
import kotlin.ranges.RangesKt;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.BlockPos;
import club.tifality.manager.event.impl.player.UpdatePositionEvent;

import java.util.concurrent.ThreadLocalRandom;

@ModuleInfo(label = "Speed", category = ModuleCategory.MOVEMENT)
public final class Speed extends Module {
    private final EnumProperty<SpeedMode> speedModeProperty = new EnumProperty<>("Mode", SpeedMode.WATCHDOG);
    private final Property<Boolean> timerValue = new Property<>("Timer", true, () -> speedModeProperty.get() == SpeedMode.WATCHDOG);
    private final DoubleProperty chocoSpeed = new DoubleProperty("Choco Speed", 0.475, () -> speedModeProperty.get() == SpeedMode.CHOCO, 0.3, 0.6, 5.0E-4);
    private final DoubleProperty watchdogSpeed = new DoubleProperty("Watchdog Speed", 0.475, () -> speedModeProperty.get() == SpeedMode.WATCHDOG, 0.475, 1.3, 5.0E-4);
    private final DoubleProperty tacoSpeed = new DoubleProperty("Taco Speed", 0.475, () -> speedModeProperty.get() == SpeedMode.TACO, 0.475, 1.3, 5.0E-4);
    private final DoubleProperty timerAmountValue = new DoubleProperty("Timer Amount", 1.6, () -> speedModeProperty.get() == SpeedMode.CHOCO || speedModeProperty.get() == SpeedMode.WATCHDOG, 0.9, 1.6, 0.05);
    private final Property<Boolean> liquidCheck = new Property<>("Liquid Check", false, () -> speedModeProperty.get() == SpeedMode.CHOCO);
    private final DoubleProperty motionYValue = new DoubleProperty("Motion Y", 0.4, () -> speedModeProperty.get() == SpeedMode.TACO, 0.0, 0.42, 0.01);
    private final EnumProperty<HopType> hopType = new EnumProperty<>("Hop Type", HopType.Normal, () -> speedModeProperty.get() == SpeedMode.TACO);
    private final EnumProperty<BoostMode> boostModeValue = new EnumProperty<>("Boost Mode", BoostMode.Yport, () -> speedModeProperty.get() == SpeedMode.TACO);
    private final Property<Boolean> stepCheck = new Property<>("Step Check", false, () -> speedModeProperty.get() == SpeedMode.TACO);
    private final DoubleProperty limitSpeedValue = new DoubleProperty("Limit Speed", 0.82, () -> speedModeProperty.get() == SpeedMode.TACO, 0.66, 0.85, 0.01);
    private final DoubleProperty strafeValue = new DoubleProperty("Strafe", 159.0, () -> speedModeProperty.get() == SpeedMode.TACO, 33.0, 159.0, 1.0);
    private final DoubleProperty downStrafeValue = new DoubleProperty("Down Strafe", 159.0, () -> speedModeProperty.get() == SpeedMode.TACO, 33.0, 159.0, 1.0);
    private final EnumProperty<Bypass> bypass = new EnumProperty<>("Bypass", Bypass.BypassOffset, () -> speedModeProperty.get() == SpeedMode.TACO);
    private final EnumProperty<Timer> timerMode = new EnumProperty<>("Timer", Timer.None, () -> speedModeProperty.get() == SpeedMode.TACO);
    private final Property<Boolean> lagBackCheckValue = new Property<>("Lag Back Check", true);
    private final Property<Boolean> noRotateSetValue = new Property<>("No Rotate Set", false);
    private final DoubleProperty stopTicksValue = new DoubleProperty("Stop Ticks", 16.0, 3.0, 25.0, 1.0);
    private final DoubleProperty normalTimerValue = new DoubleProperty("Normal Timer", 2.6, () -> speedModeProperty.get() == SpeedMode.TACO, 0.25, 5.0, 0.01);
    private final DoubleProperty groundTimerValue = new DoubleProperty("Ground Timer", 1.2, () -> speedModeProperty.get() == SpeedMode.TACO, 0.25, 5.0, 0.01);
    private final DoubleProperty fallDistValue = new DoubleProperty("Fall Dist", 255.0, () -> speedModeProperty.get() == SpeedMode.TACO, 0.0, 255.0, 1.0);
    private final DoubleProperty fallTimerValue = new DoubleProperty("Fall Timer", 0.98, () -> speedModeProperty.get() == SpeedMode.TACO, 0.25, 5.0, 0.01);
    private int stage;
    private double movementSpeed;
    private double lastDist;
    private int stopTicks;

    public Speed() {
        this.setSuffixListener(this.speedModeProperty);
    }

    public Property<Boolean> getStepCheck() {
        return this.stepCheck;
    }

    public EnumProperty<Bypass> getBypass() {
        return this.bypass;
    }

    public EnumProperty<Timer> getTimerMode() {
        return this.timerMode;
    }

    public DoubleProperty getGroundTimerValue() {
        return this.groundTimerValue;
    }


    public DoubleProperty getFallDistValue() {
        return this.fallDistValue;
    }


    public DoubleProperty getFallTimerValue() {
        return this.fallTimerValue;
    }

    private boolean isWatchdog() {
        return this.speedModeProperty.isSelected(SpeedMode.WATCHDOG);
    }

    public int getStage() {
        return this.stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.stopTicks = 0;
        mc.timer.timerSpeed = 1.0f;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.thePlayer == null) {
            return;
        }
        this.stopTicks = 0;
        mc.timer.timerSpeed = 1.0f;
        mc.thePlayer.motionX *= 0.25;
        mc.thePlayer.motionZ *= 0.25;
    }

    @Listener
    public void onUpdatePosition(UpdatePositionEvent event) {
        Step stepModule = Tifality.getInstance().getModuleManager().getModule(Step.class);
        if (this.speedModeProperty.get() == SpeedMode.TACO) {
            if (stepModule.isEnabled() && stepModule.getOnStep() && this.stage <= 2) {
                this.movementSpeed = 0.0;
            }
        }
    }

    @Listener
    public void proGaming(UpdatePositionEvent event) {
        if (this.stopTicks > 0) {
            this.stopTicks--;
            return;
        }
        if (this.speedModeProperty.isSelected(SpeedMode.TACO)) {
            if (MovementUtils.isMoving()) {
                if (mc.thePlayer.onGround) {
                    MovementUtils.fakeJump();
                }
                mc.thePlayer.setSprinting(true);
            }
            if (this.hopType.get() == HopType.Lower && !mc.thePlayer.isCollidedHorizontally) {
                if (MathUtils.round(mc.thePlayer.posY - (int)mc.thePlayer.posY, 3.0) == MathUtils.round(0.753, 3.0)) {
                    mc.thePlayer.motionY -= 0.01;
                } else if (MathUtils.round(mc.thePlayer.posY - (int)mc.thePlayer.posY, 3.0) == MathUtils.round(0.991, 3.0)) {
                    mc.thePlayer.motionY -= 0.02;
                } else if (MathUtils.round(mc.thePlayer.posY - (int)mc.thePlayer.posY, 3.0) == MathUtils.round(0.136, 3.0)) {
                    mc.thePlayer.motionY -= 0.01;
                } else if (MathUtils.round(mc.thePlayer.posY - (int)mc.thePlayer.posY, 3.0) == MathUtils.round(0.19, 3.0)) {
                    mc.thePlayer.motionY -= 0.02;
                } else if (MathUtils.round(mc.thePlayer.posY - (int)mc.thePlayer.posY, 3.0) == MathUtils.round(0.902, 3.0)) {
                    mc.thePlayer.motionY -= 0.01;
                }
            }
            if (this.hopType.get() == HopType.Higher && mc.thePlayer.fallDistance <= 1.4) {
                mc.thePlayer.motionY += 0.005574432;
            }
            GameSpeed gameSpeed = ModuleManager.getInstance(GameSpeed.class);
            Step step = ModuleManager.getInstance(Step.class);
            if (!step.getOnStep()) {
                if (!gameSpeed.isEnabled() && this.timerMode.get() != Timer.None && MovementUtils.isMoving()) {
                    if (mc.thePlayer.fallDistance > 0.0) {
                        double n = mc.thePlayer.fallDistance;
                        if (n > this.fallDistValue.getValue()) {
                            mc.timer.timerSpeed = (float)(this.normalTimerValue).get().doubleValue();
                        }
                        else {
                            mc.timer.timerSpeed = (float)(this.fallTimerValue).get().doubleValue();
                        }
                    }
                    else if (mc.thePlayer.onGround) {
                        mc.timer.timerSpeed = (float)(this.groundTimerValue).get().doubleValue();
                    }
                    else {
                        mc.timer.timerSpeed = (float)(this.normalTimerValue).get().doubleValue();
                    }
                }
            }
        }
    }

    @Listener
    public void onWhenYouSeeIt(UpdatePositionEvent event) {
        if (this.stopTicks > 0) {
            return;
        }
        if (this.speedModeProperty.isSelected(SpeedMode.TACO)) {
            if (this.bypass.get() == Bypass.SpoofGround && MovementUtils.isMoving() && mc.thePlayer.onGround) {
                event.setPosY(event.getPosY() + ThreadLocalRandom.current().nextDouble() / 1000.0);
                event.setOnGround(true);
            }
            if (this.bypass.get() == Bypass.BypassOffset) {
                MovementUtils.bypassOffSet(event);
            }
        }
    }

    @Listener
    private void chocoPie(UpdatePositionEvent e) {
        if (mc.thePlayer.isSneaking() || this.stopTicks > 0) {
            return;
        }
        if (this.speedModeProperty.isSelected(SpeedMode.TACO) && MovementUtils.isMoving()) {
            mc.thePlayer.setSprinting(true);
        }
    }

    @Listener
    public void onPreUpdate(UpdatePositionEvent event) {
        if (this.speedModeProperty.get() == SpeedMode.TACO) {
            final double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
            final double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
            this.lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
        }
    }

    @Listener
    private void onMove(MoveEntityEvent e) {
        if (!mc.thePlayer.isSneaking() && this.stopTicks <= 0) {
            Flight module = ModuleManager.getInstance(Flight.class);
            if (!module.isEnabled()) {
                return;
            }
        }
    }

    @Listener
    public void onPacket(PacketReceiveEvent event) {
        if (mc.thePlayer == null) {
            return;
        }
        if (this.lagBackCheckValue.get() && event.getPacket() instanceof S08PacketPlayerPosLook) {
            mc.thePlayer.motionX *= 0.0;
            mc.thePlayer.motionY *= 0.0;
            mc.thePlayer.motionZ *= 0.0;
            this.stopTicks = (int)this.stopTicksValue.get().doubleValue();
            if (this.noRotateSetValue.get()) {
                Packet<?> packet = event.getPacket();
                if (packet == null) {
                    throw new NullPointerException("null cannot be cast to non-null type net.minecraft.network.play.server.S08PacketPlayerPosLook");
                }
                S08PacketPlayerPosLook s08PacketPlayerPosLook = (S08PacketPlayerPosLook)packet;
            }
        }
    }

    @Listener(Priority.LOW)
    private void onMoveEntityEvent(MoveEntityEvent e) {
        double baseSpeed = MovementUtils.getBaseMoveSpeed(0.2873);
        if (this.speedModeProperty.get() == SpeedMode.CHOCO) {
            if (MovementUtils.isMoving()) {
                if (this.liquidCheck.get()) {
                    if (!mc.thePlayer.isInWater()) {
                        if (!mc.thePlayer.isInLava()) {
                            GameSpeed gameSpeed = ModuleManager.getInstance(GameSpeed.class);
                            if (!gameSpeed.isEnabled()) {
                                mc.timer.timerSpeed = (float)this.timerAmountValue.get().doubleValue();
                            }
                            IBlockState blockPos = Wrapper.getWorld().getBlockState(new BlockPos(Wrapper.getPlayer().posX, Wrapper.getPlayer().posY, Wrapper.getPlayer().posZ));
                            if (blockPos.getBlock() instanceof BlockStairs) {
                                MovementUtils.setMotion(MovementUtils.getBaseMoveSpeed(0.2873));
                            } else if (Wrapper.getPlayer().onGround) {
                                mc.thePlayer.motionY = MovementUtils.getJumpBoostModifier(0.39999998, true);
                                double speed = ((Number) this.chocoSpeed.getValue()).doubleValue() + (double) MovementUtils.getSpeedEffect() * 0.1;
                                Scaffold scaffold = ModuleManager.getInstance(Scaffold.class);
                                MovementUtils.setMotion(RangesKt.coerceAtLeast(speed * (scaffold.isEnabled() ? 0.66 : 1.0), baseSpeed));
                            } else {
                                MovementUtils.setMotion(MovementUtils.getSpeed());
                            }
                        }
                    }
                }
            }
        } else if (this.speedModeProperty.get() == SpeedMode.WATCHDOG) {
            if (mc.thePlayer == null) {
                return;
            }
            if (MovementUtils.isMoving() && mc.thePlayer.isCollidedHorizontally) {
                MovementUtils.setMotio(e, MovementUtils.getBaseMoveSpeed(0.258));
            }
        } else if (this.speedModeProperty.get() == SpeedMode.TACO) {
            if (mc.thePlayer == null) {
                return;
            }

            Scaffold scaffoldModule = Tifality.getInstance().getModuleManager().getModule(Scaffold.class);
            double normalSpeed = this.tacoSpeed.get() * MovementUtils.getBaseMoveSpeed();
            double bhopSpeed = this.tacoSpeed.get() * MovementUtils.getBaseMoveSpeed();
            double lowhopSpeed = this.tacoSpeed.get() * MovementUtils.getBaseMoveSpeed();
            double yportSpeed = this.tacoSpeed.get() * MovementUtils.getBaseMoveSpeed();
            boolean slowDown = mc.thePlayer.fallDistance > 0.0;
            if (MovementUtils.isMoving()) {
                if (this.stepCheck.get()) {
                    mc.thePlayer.stepHeight = 0.6F;
                } else {
                    mc.thePlayer.stepHeight = 1.0F;
                }
                switch (this.stage) {
                    case 2:
                        if (mc.thePlayer.onGround) {
                            if (this.boostModeValue.get() != null) {
                                double y;
                                switch (this.stage) {
                                    case 1:
                                        y = MovementUtils.getJumpBoostModifier(0.39999998, true);
                                        mc.thePlayer.motionY = y;
                                        break;
                                    case 2:
                                        y = MovementUtils.getJumpBoostModifier(0.16, false);
                                        mc.thePlayer.motionY = y;
                                        break;
                                    case 3:
                                        y = MovementUtils.getJumpBoostModifier(0.1, false);
                                        mc.thePlayer.motionY = y;
                                        break;
                                }
                            }

                            double motionY = MovementUtils.getJumpBoostModifier(this.motionYValue.get(), true);
                            e.setY(motionY);
                        }

                        double speed;

                        if (boostModeValue.get() != null) {
                            switch (this.stage) {
                                case 1:
                                    speed = MovementUtils.isOnIce()
                                            ? 1.12 * bhopSpeed
                                            : (MovementUtils.isInLiquid() ? 0.5 * bhopSpeed : (mc.thePlayer.isInLava() ? 0.25 * bhopSpeed : bhopSpeed));

                                case 2:
                                    speed = MovementUtils.isOnIce()
                                            ? 1.12 * lowhopSpeed
                                            : (MovementUtils.isInLiquid() ? 0.5 * lowhopSpeed : (mc.thePlayer.isInLava() ? 0.25 * lowhopSpeed : lowhopSpeed));

                                case 3:
                                    speed = MovementUtils.isOnIce()
                                            ? 1.12 * yportSpeed
                                            : (MovementUtils.isInLiquid() ? 0.5 * yportSpeed : (mc.thePlayer.isInLava() ? 0.25 * yportSpeed : yportSpeed));
                            }
                        }

                        speed = MovementUtils.isOnIce()
                                ? 1.12 * normalSpeed
                                : (MovementUtils.isInLiquid() ? 0.5 * normalSpeed : (mc.thePlayer.isInLava() ? 0.25 * normalSpeed : normalSpeed));


                        this.movementSpeed = speed;
                    case 3:
                        double difference = ((Number) this.limitSpeedValue.get()).doubleValue() * (this.lastDist - MovementUtils.getBaseMoveSpeed());
                        this.movementSpeed = this.lastDist - difference;
                        if (this.timerMode.get() == Timer.None) {
                            mc.timer.timerSpeed = 1.07F;
                        }
                }

                if (MovementUtils.isOnGround(-mc.thePlayer.motionY) || mc.thePlayer.isCollidedVertically && mc.thePlayer.onGround) {
                    this.stage = 1;
                }

                double strafe = slowDown ? this.downStrafeValue.get() : this.strafeValue.get();
                this.movementSpeed = this.lastDist - this.lastDist / strafe;

                this.movementSpeed = Math.max(this.movementSpeed, MovementUtils.getBaseMoveSpeed());
                MovementUtils.setMotion(e, scaffoldModule.isEnabled() ? this.movementSpeed * 0.5 : this.movementSpeed, 1.0);
                ++this.stage;
            }
        }
    }

    @Listener
    private void onUpdate(UpdatePositionEvent e) {
        if (this.speedModeProperty.get() == SpeedMode.WATCHDOG) {
            if (mc.thePlayer == null) {
                return;
            }
            Scaffold scaffoldModule = ModuleManager.getInstance(Scaffold.class);
            GameSpeed timer = ModuleManager.getInstance(GameSpeed.class);
            IBlockState blockPos = Wrapper.getWorld().getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));
            if (MovementUtils.isMoving()) {
                if (blockPos.getBlock() instanceof BlockStairs) {
                    MovementUtils.setMotion(MovementUtils.getBaseMoveSpeed(0.2873));
                } else if (mc.thePlayer.onGround) {
                    mc.thePlayer.motionY = MovementUtils.getJumpBoostModifier(0.39999998, true);
                    final double n = (this.watchdogSpeed).get() + MovementUtils.getSpeedEffect() * 0.1;
                    MovementUtils.setMotion(Math.max(n * (scaffoldModule.isEnabled() ? 0.66 : 1.0), MovementUtils.getBaseMoveSpeed(0.2873)));
                } else {
                    if (!timer.isEnabled()) {
                        mc.timer.timerSpeed = 1.07f;
                    } else {
                        if (timerValue.get()) {
                            mc.timer.timerSpeed = (float)(this.timerAmountValue).get().doubleValue();
                        }
                    }
                    MovementUtils.setMotion(MovementUtils.getSpeed());
                }
            } else {
                mc.thePlayer.motionX *= 0;
                mc.thePlayer.motionZ *= 0;
            }
        }
    }

    public enum SpeedMode {
        WATCHDOG,
        TACO,
        CHOCO;
    }

    public enum HopType {
        Normal,
        Lower,
        Higher;
    }

    public enum BoostMode {
        Normal,
        Bhop,
        LowHop,
        Yport;
    }

    public enum Bypass {
        SpoofGround,
        BypassOffset,
        None;
    }

    public enum Timer {
        Custom,
        Stage,
        None;
    }
}
