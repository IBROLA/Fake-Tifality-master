package club.tifality.module.impl.combat;

import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.event.impl.entity.RayTraceEntityEvent;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.property.impl.DoubleProperty;

@ModuleInfo(label = "Reach", category = ModuleCategory.COMBAT)
public final class Reach extends Module {

    private final DoubleProperty reachProperty = new DoubleProperty("Reach", 3.5, 3.0, 6.0, 0.05);

    @Listener
    public void onRayTraceEntity(RayTraceEntityEvent event) {
        event.setReach(reachProperty.getValue().floatValue());
    }
}
