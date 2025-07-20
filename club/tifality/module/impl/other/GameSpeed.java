package club.tifality.module.impl.other;

import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.event.impl.player.UpdatePositionEvent;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.property.Property;
import club.tifality.property.impl.DoubleProperty;
import club.tifality.property.impl.EnumProperty;
import club.tifality.utils.timer.TimerUtil;
import club.tifality.utils.movement.MovementUtils;

@ModuleInfo(label = "GameSpeed", category = ModuleCategory.OTHER)
public final class GameSpeed extends Module {
    private final EnumProperty<TimerMode> modeValue = new EnumProperty<>("Mode", TimerMode.TICK);
    private final DoubleProperty tickValue = new DoubleProperty("Ticks Existed", 1.0, 1.0, 5.0, 1.0);
    private final DoubleProperty maxTimerValue = new DoubleProperty("Max Timer", 1.5, 0.1, 2.5, 0.1);
    private final DoubleProperty minTimerValue = new DoubleProperty("Min Timer", 1.3, 0.1, 2.5, 0.1);
    private final Property<Boolean> onMoveValue = new Property<>("On Move", true);
    private boolean stage;
    private final TimerUtil timer = new TimerUtil();
    
    @Override
    public void onEnable() {
        if (mc.thePlayer == null) {
            return;
        }
        mc.timer.timerSpeed = (float)this.maxTimerValue.get().doubleValue();
    }
    
    @Override
    public void onDisable() {
        if (mc.thePlayer == null) {
            return;
        }
        mc.timer.timerSpeed = 1.0f;
    }
    
    @Listener
    public void onUpdate(UpdatePositionEvent event) {
        if (MovementUtils.isMoving() || !this.onMoveValue.get()) {
            switch (this.modeValue.get().ordinal()) {
                case 1: {
                    if (mc.thePlayer.ticksExisted % (int)this.tickValue.get().doubleValue() == 0) {
                        mc.timer.timerSpeed = (float)this.maxTimerValue.get().doubleValue();
                        break;
                    }
                    break;
                }
                case 2: {
                    if (!this.stage) {
                        mc.timer.timerSpeed = (float)this.maxTimerValue.get().doubleValue();
                        if (this.timer.hasElapsed(400L)) {
                            this.timer.reset();
                            this.stage = !this.stage;
                        }
                        break;
                    }
                    mc.timer.timerSpeed = (float)this.minTimerValue.get().doubleValue();
                    if (this.timer.hasElapsed(550L)) {
                        this.timer.reset();
                        this.stage = !this.stage;
                        break;
                    }
                    break;
                }
            }
            return;
        }
        mc.timer.timerSpeed = 1.0f;
    }

    public enum TimerMode {
        TICK, 
        TIMER;
    }
}
