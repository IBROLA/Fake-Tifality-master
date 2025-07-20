package club.tifality.module.impl.render;

import club.tifality.gui.font.FontManager;
import club.tifality.gui.font.TrueTypeFontRenderer;
import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.event.impl.entity.EntityDeathEvent;
import club.tifality.manager.event.impl.entity.SpawnParticleEntityEvent;
import club.tifality.manager.event.impl.player.DamageEntityEvent;
import club.tifality.manager.event.impl.render.overlay.Render2DEvent;
import club.tifality.manager.event.impl.world.WorldLoadEvent;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.utils.render.OGLUtils;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.opengl.GL11;
import club.tifality.manager.event.impl.render.Render3DEvent;
import club.tifality.module.Module;

import java.util.*;

import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(label = "Damage Particles", category = ModuleCategory.RENDER)
public final class DamageParticles extends Module {

    private final List<DamageEvent> damageEvents = new ArrayList<>();
    private final Map<DamageEvent, float[]> damagePosMap = new HashMap<>();
    private final List<Entity> recentlyCrit = new ArrayList<>();
    private final Map<Entity, Float> recentlyDied = new HashMap<>();

    @Listener
    public void onRender2D(Render2DEvent event) {
        final TrueTypeFontRenderer fr = FontManager.FN_FR;
        final long current = System.currentTimeMillis();

        OGLUtils.enableBlending();

        for (DamageEvent damageEvent : this.damageEvents) {
            final float[] pos = this.damagePosMap.get(damageEvent);

            if (pos != null) {
                final long timeSinceCreated = current - damageEvent.time;
                final boolean fadingOut = timeSinceCreated > damageEvent.duration - damageEvent.fadeOutDuration;

                float fadeOutPerc = 0.0F;

                if (fadingOut) {
                    final long fadeOutProgress = damageEvent.fadeOutDuration - (damageEvent.duration - timeSinceCreated);
                    fadeOutPerc = fadeOutProgress / (float) damageEvent.fadeOutDuration;

                    GL11.glColor4f(1, 1, damageEvent.crit ? 0 : 1, 1 - fadeOutPerc);
                } else {
                    GL11.glColor4f(1, 1, damageEvent.crit ? 0 : 1, 1);
                }


                final String damage = damageEvent.damage;
                float x = pos[0];
                final float dmgypos = fr.getHeight(damageEvent.damage);
                final float ypos = pos[1];
                for (int i = 0; i < damage.length(); i++) {
                    if (fadingOut) {
                        final boolean rightChar = i == 1;
                        final float rightOffset = rightChar ? fr.getWidth(damageEvent.damage) : 0;
                        GL11.glPushMatrix();
                        GL11.glTranslatef(x + rightOffset, ypos + dmgypos + fadeOutPerc * 150.0F, 0);
                        GL11.glRotatef(135.0F * fadeOutPerc, 0, 0, rightChar ? 1 : -1);
                        GL11.glTranslatef(-rightOffset, -dmgypos, 0);
                    }

                    final TrueTypeFontRenderer.CharacterData charData = fr.charData[damage.charAt(i)];

                    if (fadingOut) {
                        charData.bind();
                        glBegin(GL_QUADS);
                        {
                            glTexCoord2f(0, 0);
                            glVertex2d(0, 0);
                            glTexCoord2f(0, 1);
                            glVertex2d(0, charData.height);
                            glTexCoord2f(1, 1);
                            glVertex2d(charData.width, charData.height);
                            glTexCoord2f(1, 0);
                            glVertex2d(charData.width, 0);
                        }
                        glEnd();
                    } else {
                        fr.drawChar(charData, x, ypos);
                    }
                    x += charData.width - 12;

                    if (fadingOut)
                        GL11.glPopMatrix();
                }
            }
        }

        GL11.glDisable(GL11.GL_BLEND);
    }

    @Listener
    public void onWorldLoad(WorldLoadEvent event) {
        this.damageEvents.clear();
        this.damagePosMap.clear();
        this.recentlyCrit.clear();
        this.recentlyDied.clear();
    }

    @Listener
    public void onSpawnParticle(SpawnParticleEntityEvent event) {
        final Entity entity = event.getEntity();

        for (DamageEvent damageEvent : this.damageEvents) {
            if (damageEvent.crit)
                continue;
            if (entity == damageEvent.entity) {
                damageEvent.crit = true;
                return;
            }
        }

        this.recentlyCrit.add(entity);
    }

    @Listener
    public void onEntityDie(EntityDeathEvent event) {
        this.recentlyDied.put(event.getEntity(), event.getOldHealth());
    }

    @Listener
    public void onDamageEntity(DamageEntityEvent event) {
        final EntityLivingBase entity = event.getEntity();
        double damage = event.getDamage();

        if (damage == 0)
            damage = this.recentlyDied.remove(entity);

        this.damageEvents.add(new DamageEvent(entity, System.currentTimeMillis(), 1000, 700, String.valueOf((int) Math.ceil(damage * 5.0)), (float) entity.posX, (float) (entity.getEntityBoundingBox().expand(0.1F, 0.1F, 0.1F).maxY), (float) entity.posZ, this.recentlyCrit.remove(entity)));
    }

    @Listener
    public void onRender3D(Render3DEvent event) {
        final List<DamageEvent> events = this.damageEvents;
        final long current = System.currentTimeMillis();

        for (int i = 0, eventsSize = events.size(); i < eventsSize; i++) {
            final DamageEvent damageEvent = events.get(i);
            final long timeSinceCreated = current - damageEvent.time;
            if (timeSinceCreated > damageEvent.duration) {
                events.remove(i);
                eventsSize--;
                i--;
                continue;
            }

            final float[] projection = OGLUtils.project2D(
                    (float) (damageEvent.x - RenderManager.viewerPosX),
                    (float) (damageEvent.y - RenderManager.viewerPosY),
                    (float) (damageEvent.z - RenderManager.viewerPosZ),
                    2);

            if (projection != null && projection[2] >= 0.0F && projection[2] < 1.0F) {
                this.damagePosMap.put(damageEvent, projection);
            }
        }
    }
    /*@EventLink
    public final Listener<Render2DEvent> onRender2D = renderEvent -> {
        final TrueTypeFontRenderer fr = FontManager.FN_FR;
        final long current = System.currentTimeMillis();

        OGLUtils.enableBlending();

        for (DamageEvent event : this.damageEvents) {
            final float[] pos = this.damagePosMap.get(event);

            if (pos != null) {
                final long timeSinceCreated = current - event.time;
                final boolean fadingOut = timeSinceCreated > event.duration - event.fadeOutDuration;

                float fadeOutPerc = 0.0F;

                if (fadingOut) {
                    final long fadeOutProgress = event.fadeOutDuration - (event.duration - timeSinceCreated);
                    fadeOutPerc = fadeOutProgress / (float) event.fadeOutDuration;

                    GL11.glColor4f(1, 1, event.crit ? 0 : 1, 1 - fadeOutPerc);
                } else {
                    GL11.glColor4f(1, 1, event.crit ? 0 : 1, 1);
                }


                final String damage = event.damage;
                float x = pos[0];
                final float dmgypos = fr.getHeight(event.damage);
                final float ypos = pos[1];
                for (int i = 0; i < damage.length(); i++) {
                    if (fadingOut) {
                        final boolean rightChar = i == 1;
                        final float rightOffset = rightChar ? fr.getWidth(event.damage) : 0;
                        GL11.glPushMatrix();
                        GL11.glTranslatef(x + rightOffset, ypos + dmgypos + fadeOutPerc * 150.0F, 0);
                        GL11.glRotatef(135.0F * fadeOutPerc, 0, 0, rightChar ? 1 : -1);
                        GL11.glTranslatef(-rightOffset, -dmgypos, 0);
                    }

                    final TrueTypeFontRenderer.CharacterData charData = fr.charData[damage.charAt(i)];

                    if (fadingOut) {
                        charData.bind();
                        glBegin(GL_QUADS);
                        {
                            glTexCoord2f(0, 0);
                            glVertex2d(0, 0);
                            glTexCoord2f(0, 1);
                            glVertex2d(0, charData.height);
                            glTexCoord2f(1, 1);
                            glVertex2d(charData.width, charData.height);
                            glTexCoord2f(1, 0);
                            glVertex2d(charData.width, 0);
                        }
                        glEnd();
                    } else {
                        fr.drawChar(charData, x, ypos);
                    }
                    x += charData.width - 12;

                    if (fadingOut)
                        GL11.glPopMatrix();
                }
            }
        }

        GL11.glDisable(GL11.GL_BLEND);
    };

    @EventLink
    public final Listener<WorldLoadEvent> onWorldLoad = event -> {
        this.damageEvents.clear();
        this.damagePosMap.clear();
        this.recentlyCrit.clear();
        this.recentlyDied.clear();
    };

    @EventLink
    public final Listener<SpawnParticleEntityEvent> onSpawnParticle = particleEvent -> {
        final Entity entity = particleEvent.getEntity();

        for (DamageEvent event : this.damageEvents) {
            if (event.crit)
                continue;
            if (entity == event.entity) {
                event.crit = true;
                return;
            }
        }

        this.recentlyCrit.add(entity);
    };

    @EventLink
    public final Listener<EntityDeathEvent> onEntityDie = event -> {
        this.recentlyDied.put(event.getEntity(), event.getOldHealth());
    };

    @EventLink
    public final Listener<EntityHealthUpdateEvent> onDamageEntity = event -> {
        final EntityLivingBase entity = event.getEntity();
        double damage = event.getDamage();

        if (damage == 0)
            damage = this.recentlyDied.remove(entity);

        this.damageEvents.add(new DamageEvent(entity, System.currentTimeMillis(), 1000, 700, String.valueOf((int) Math.ceil(damage * 5.0)), (float) entity.posX, (float) (entity.getEntityBoundingBox().expand(0.1F, 0.1F, 0.1F).maxY), (float) entity.posZ, this.recentlyCrit.remove(entity)));
    };

    @EventLink
    private final Listener<Render3DEvent> onRender3D = renderEvent -> {
        final List<DamageEvent> events = this.damageEvents;
        final long current = System.currentTimeMillis();

        for (int i = 0, eventsSize = events.size(); i < eventsSize; i++) {
            final DamageEvent event = events.get(i);
            final long timeSinceCreated = current - event.time;
            if (timeSinceCreated > event.duration) {
                events.remove(i);
                eventsSize--;
                i--;
                continue;
            }

            final float[] projection = OGLUtils.project2D(
                    (float) (event.x - RenderManager.viewerPosX),
                    (float) (event.y - RenderManager.viewerPosY),
                    (float) (event.z - RenderManager.viewerPosZ),
                    2);

            if (projection != null && projection[2] >= 0.0F && projection[2] < 1.0F) {
                this.damagePosMap.put(event, projection);
            }
        }
    };*/

    private static class DamageEvent {
        private final Entity entity;
        private final long time, duration, fadeOutDuration;
        private final String damage;
        private final float x, y, z;
        private boolean crit;

        public DamageEvent(Entity entity, long time, long duration, long fadeOutDuration, String damage, float x, float y, float z, boolean crit) {
            this.entity = entity;
            this.time = time;
            this.duration = duration;
            this.fadeOutDuration = fadeOutDuration;
            this.damage = damage;
            this.x = x;
            this.y = y;
            this.z = z;
            this.crit = crit;
        }

        @Override
        public int hashCode() {
            return Objects.hash(damage, x, y, z);
        }
    }

}
