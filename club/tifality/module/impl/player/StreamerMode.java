package club.tifality.module.impl.player;

import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.event.impl.render.overlay.RenderBossHealthEvent;
import club.tifality.manager.event.impl.render.overlay.RenderGuiTabPlayerEvent;
import club.tifality.manager.event.impl.render.overlay.RenderScoreboardEvent;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.property.Property;
import club.tifality.module.Module;

@ModuleInfo(label="StreamerMode", category= ModuleCategory.PLAYER)
public final class StreamerMode extends Module {
    public static Property<Boolean> skinSpoof = new Property<>("Skin spoof", false);
    public static Property<Boolean> hideBossbar = new Property<>("Hide Boss health", false);
    public static Property<Boolean> hideScoreboard = new Property<>("Hide scoreboard", false);
    public static Property<Boolean> hideTab = new Property<>("Hide tab", false);

    @Listener
    private void onRenderBossHealthEvent(RenderBossHealthEvent event) {
        if (hideBossbar.get()) {
            event.setCancelled();
        }
    }

    @Listener
    private void onRenderScoreboardEvent(RenderScoreboardEvent event) {
        if (hideScoreboard.get()) {
            event.setCancelled();
        }
    }

    @Listener
    private void onRenderGuiTabPlayerEvent(RenderGuiTabPlayerEvent event) {
        if (hideTab.get()) {
            event.setCancelled();
        }
    }
}

