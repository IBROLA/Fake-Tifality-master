package club.tifality.manager.event.impl.entity;

import net.minecraft.entity.EntityLivingBase;
import club.tifality.manager.event.Event;

public class EntitySwingEvent implements Event {

    private final int entityId;

    public EntitySwingEvent(EntityLivingBase entity) {
        this.entityId = entity.getEntityId();
    }

    public int getEntityId() {
        return entityId;
    }

}
