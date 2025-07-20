package club.tifality.module.impl.movement;

import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.property.impl.DoubleProperty;
import club.tifality.property.impl.Representation;

@ModuleInfo(label="Phase", category= ModuleCategory.MOVEMENT)
public class Phase extends Module {
    private final DoubleProperty hypixelYValue = new DoubleProperty("Y", 5.0, 1.0, 10.0, 1.0, Representation.DISTANCE);

    @Override
    public void onEnable() {
        super.onEnable();
        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - this.hypixelYValue.get(), mc.thePlayer.posZ);
        this.setEnabled(false);
    }
}

