package club.tifality.module.impl.other;

import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.module.ModuleManager;

@ModuleInfo(label = "Memory Fix", category = ModuleCategory.OTHER)
public final class MemoryFix extends Module {

    public MemoryFix() {
        toggle();
        setHidden(true);
    }

    public static boolean cancelGarbageCollection() {
        return ModuleManager.getInstance(MemoryFix.class).isEnabled();
    }
}
