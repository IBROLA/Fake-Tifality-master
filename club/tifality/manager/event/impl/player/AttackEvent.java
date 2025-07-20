package club.tifality.manager.event.impl.player;

import net.minecraft.entity.EntityLivingBase;
import club.tifality.manager.event.CancellableEvent;

public final class AttackEvent extends CancellableEvent {
    private final EntityLivingBase entity;

    public AttackEvent(EntityLivingBase entity) {
        this.entity = entity;
    }

    public EntityLivingBase getEntity() {
        return this.entity;
    }
}

