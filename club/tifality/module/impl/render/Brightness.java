package club.tifality.module.impl.render;

import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.event.impl.world.TickEvent;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;

@ModuleInfo(label="Brightness", category=ModuleCategory.RENDER)
public final class Brightness extends Module {
    @Listener
    public void onEvent(TickEvent event) {
        mc.thePlayer.addPotionEffect(new PotionEffect(Potion.nightVision.getId(), 5200, 1));
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mc.thePlayer.removePotionEffect(Potion.nightVision.getId());
    }
}