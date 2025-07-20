package club.tifality.module.impl.combat;

import club.tifality.manager.api.annotations.Listener;
import net.minecraft.network.play.client.C03PacketPlayer;
import club.tifality.manager.event.impl.player.UpdatePositionEvent;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.property.impl.DoubleProperty;
import club.tifality.utils.movement.MovementUtils;

@ModuleInfo(label = "Regen", category = ModuleCategory.COMBAT)
public final class Regen extends Module {

    private final DoubleProperty packetsProperty = new DoubleProperty("Packets", 10, 0, 100, 1);

    @Listener
    public void onUpdatePositionEvent(UpdatePositionEvent event) {
        if (event.isPre() && MovementUtils.isOnGround() && mc.thePlayer.getHealth() < mc.thePlayer.getMaxHealth())
            for (int i = 0; i < packetsProperty.getValue().intValue(); i++)
                mc.getNetHandler().sendPacket(new C03PacketPlayer(true));
    }
    /*@EventLink
    public final Listener<UpdatePositionEvent> onUpdatePositionEvent = event -> {
        if (event.isPre() && MovementUtils.isOnGround() && Wrapper.getPlayer().getHealth() < Wrapper.getPlayer().getMaxHealth())
            for (int i = 0; i < packetsProperty.getValue().intValue(); i++)
                Wrapper.sendPacketDirect(new C03PacketPlayer(true));
    };*/
}
