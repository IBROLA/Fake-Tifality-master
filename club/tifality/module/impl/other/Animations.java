package club.tifality.module.impl.other;

import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.event.impl.player.AttackEvent;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.module.ModuleManager;
import club.tifality.property.Property;
import club.tifality.property.impl.DoubleProperty;
import club.tifality.property.impl.EnumProperty;
import club.tifality.utils.Wrapper;
import club.tifality.utils.timer.TimerUtil;

@ModuleInfo(label="Animations", category=ModuleCategory.OTHER)
public final class Animations extends Module {
    public final EnumProperty<AnimationMode> animationModeProperty = new EnumProperty<>("Mode", AnimationMode.OLD);
    public final Property<Boolean> coronaValue = new Property<>("Corona", true, () -> this.animationModeProperty.get() == AnimationMode.CORONA);
    public final Property<Boolean> equipProgressProperty = new Property<>("Duration", true);
    private final Property<Boolean> forceUpdateEquip = new Property<>("Force update", false);
    public final DoubleProperty equipProgMultProperty = new DoubleProperty("Progress", 2.0, this.equipProgressProperty::get, 0.5, 3.0, 0.1);
    public final DoubleProperty itemScale = new DoubleProperty("Item Scale", 1.0, 0.0, 1.0, 0.05);
    public final DoubleProperty swingSpeed = new DoubleProperty("Speed", 1.0, 0.1, 2.0, 0.1);
    public final DoubleProperty xPosProperty = new DoubleProperty("X", 0.0, -1.0, 1.0, 0.05);
    public final DoubleProperty yPosProperty = new DoubleProperty("Y", 0.0, -1.0, 1.0, 0.05);
    public final DoubleProperty zPosProperty = new DoubleProperty("Z", 0.0, -1.0, 1.0, 0.05);
    private final TimerUtil delay = new TimerUtil();

    public static Animations getInstance() {
        return ModuleManager.getInstance(Animations.class);
    }

    public Animations() {
        this.toggle();
        this.setHidden(true);
    }

    @Listener
    public void onAttack(AttackEvent event) {
        if (this.forceUpdateEquip.get() && mc.thePlayer.isBlocking() && this.delay.hasElapsed(500)) {
            mc.getItemRenderer().resetEquippedProgress();
            delay.reset();
        }
    }

    public enum AnimationMode {
        OLD,
        AVATAR,
        JELLO,
        LUCKY,
        SLIDE,
        CORONA,
        SWONG,
        SWUNG,
        SWACK,
        SWONK,
        SWANG,
        SWANK,
        DEV;
    }
}

