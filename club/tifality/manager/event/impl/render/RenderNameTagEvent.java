package club.tifality.manager.event.impl.render;

import club.tifality.manager.event.CancellableEvent;
import net.minecraft.entity.EntityLivingBase;

public final class RenderNameTagEvent extends CancellableEvent {

    private final EntityLivingBase entityLivingBase;

    public RenderNameTagEvent(EntityLivingBase entityLivingBase) {
        this.entityLivingBase = entityLivingBase;
    }

    public EntityLivingBase getEntityLivingBase() {
        return entityLivingBase;
    }

}
