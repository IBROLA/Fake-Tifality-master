package club.tifality.manager.event.impl.player;

import net.minecraft.entity.EntityLivingBase;
import club.tifality.manager.event.Event;

public final class DamageEntityEvent implements Event {

    private final EntityLivingBase entity;
    private final double damage;

    public DamageEntityEvent(EntityLivingBase entity, double damage) {
        this.entity = entity;
        this.damage = damage;
    }

    public EntityLivingBase getEntity() {
        return entity;
    }

    public double getDamage() {
        return damage;
    }

}
