package club.tifality.module.impl.combat;

import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.event.impl.world.TickEvent;
import org.apache.commons.lang3.RandomUtils;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.property.Property;
import club.tifality.property.impl.DoubleProperty;
import club.tifality.utils.timer.TimerUtil;

@ModuleInfo(label = "Auto Clicker", category = ModuleCategory.COMBAT)
public final class AutoClicker extends Module {

    private final DoubleProperty minApsProperty = new DoubleProperty("Min APS", 9.0, 1.0,
            20.0, 0.1);
    private final DoubleProperty maxApsProperty = new DoubleProperty("Max APS", 12.0, 1.0,
            20.0, 0.1);
    private final Property<Boolean> rightClickProperty = new Property<>("Right Click", false);

    private final TimerUtil cpsTimer = new TimerUtil();

    @Listener
    public void onTickEvent(TickEvent event) {
        if (rightClickProperty.getValue() && mc.gameSettings.keyBindUseItem.isKeyDown()) {
            mc.rightClickMouse();
        } else if (mc.gameSettings.keyBindAttack.isKeyDown() && !mc.thePlayer.isUsingItem()) {
            final int cps = RandomUtils.nextInt(minApsProperty.getValue().intValue(), maxApsProperty.getValue().intValue());
            if (cpsTimer.hasElapsed(1000 / cps)) {
                mc.clickMouse();
                cpsTimer.reset();
            }
        }
    }
}
