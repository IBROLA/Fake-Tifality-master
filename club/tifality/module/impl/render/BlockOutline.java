package club.tifality.module.impl.render;

import club.tifality.utils.render.Colors;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.module.ModuleManager;
import club.tifality.property.Property;

@ModuleInfo(label="BlockOutline", category= ModuleCategory.RENDER)
public final class BlockOutline extends Module {
    private final Property<Integer> blockOutlineColorProperty = new Property<>("Outline Color", Colors.PURPLE);

    public static float getOutlineAlpha() {
        return (float)(ModuleManager.getInstance(BlockOutline.class).blockOutlineColorProperty.getValue() >> 25 & 0xFF) / 255.0f;
    }

    public static int getOutlineColor() {
        return ModuleManager.getInstance(BlockOutline.class).blockOutlineColorProperty.getValue();
    }

    public static boolean isOutlineActive() {
        return ModuleManager.getInstance(BlockOutline.class).isEnabled();
    }
}