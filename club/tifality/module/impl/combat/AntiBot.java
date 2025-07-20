package club.tifality.module.impl.combat;

import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.event.impl.player.DamageEntityEvent;
import club.tifality.manager.event.impl.world.WorldLoadEvent;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.property.impl.EnumProperty;
import club.tifality.utils.PlayerUtils;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(label = "AntiBot", category = ModuleCategory.COMBAT)
public final class AntiBot extends Module {

    private final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.WATCHDOG);
    private final List<Entity> validEntities = new ArrayList<>();

    @Listener
    public void onWorldLoad(WorldLoadEvent event) {
        this.validEntities.clear();
    }

    @Listener
    public void onEntityHealthUpdate(DamageEntityEvent event) {
        if (event.getEntity() instanceof EntityOtherPlayerMP)
            this.validEntities.add(event.getEntity());
    }

    public AntiBot() {
        setSuffixListener(mode);
    }

    public boolean isBot(final EntityPlayer player) {
        return this.isEnabled() && !this.validEntities.contains(player) && this.mode.getValue().botCheck.check(player);
    }

    private enum Mode {
        WATCHDOG(PlayerUtils::hasInvalidNetInfo);

        private final CheckPlayer botCheck;

        Mode(CheckPlayer botCheck) {
            this.botCheck = botCheck;
        }
    }

    @FunctionalInterface
    public interface CheckPlayer {
        boolean check(EntityPlayer player);
    }
}
