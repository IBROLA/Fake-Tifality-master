package club.tifality.module.impl.render;

import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.module.ModuleManager;
import club.tifality.property.impl.EnumProperty;

@ModuleInfo(label = "TimeChanger", category = ModuleCategory.RENDER)
public final class TimeChanger extends Module {

    private final EnumProperty<Time> time = new EnumProperty<>("World Time", Time.MORNING);

    public static boolean shouldChangeTime() {
        return ModuleManager.getInstance(TimeChanger.class).isEnabled();
    }

    public static int getWorldTime() {
        return ModuleManager.getInstance(TimeChanger.class).time.getValue().worldTicks;
    }

    public TimeChanger() {
        setHidden(true);
        toggle();
    }

    private enum Time {
        NIGHT(13000),
        MIDNIGHT(18000),
        MORNING(23000),
        DAY(1000);

        private final int worldTicks;

        Time(int worldTicks) {
            this.worldTicks = worldTicks;
        }
    }

}
