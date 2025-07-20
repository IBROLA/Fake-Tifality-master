package club.tifality.module.impl.other;

import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.property.Property;

import java.awt.*;

@ModuleInfo(label="SilentView", category= ModuleCategory.OTHER)
public final class SilentView extends Module {
    public static final Property<Boolean> ghostSilentView = new Property<>("Ghost", false);
    public static final Property<Integer> color = new Property<>("Color", new Color(255, 0, 0).getRGB(), ghostSilentView::get);
}

