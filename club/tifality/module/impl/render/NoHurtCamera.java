package club.tifality.module.impl.render;

import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.event.impl.render.HurtShakeEvent;
import club.tifality.manager.event.impl.render.ViewClipEvent;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.property.Property;

@ModuleInfo(label = "ViewClip", category = ModuleCategory.RENDER)
public final class NoHurtCamera extends Module {

    private final Property<Boolean> noHurtShakeProperty = new Property<>("Hurt Shake", true);
    private final Property<Boolean> viewClipProperty = new Property<>("View Clip", true);

    @Listener
    public void onViewClipEvent(ViewClipEvent event) {
        if (viewClipProperty.getValue())
            event.setCancelled();
    }

    @Listener
    public void onHurtShakeEvent(HurtShakeEvent event) {
        if (noHurtShakeProperty.getValue())
            event.setCancelled();
    }

}
