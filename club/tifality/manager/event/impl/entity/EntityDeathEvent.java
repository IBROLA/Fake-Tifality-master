package club.tifality.manager.event.impl.entity;

import net.minecraft.entity.Entity;
import club.tifality.manager.event.Event;

public final class EntityDeathEvent implements Event {

    private final Entity entity;
    private final float oldHealth;

    public EntityDeathEvent(Entity entity, float oldHealth) {
        this.entity = entity;
        this.oldHealth = oldHealth;
    }

    public Entity getEntity() {
        return entity;
    }

    public float getOldHealth() {
        return oldHealth;
    }

}
