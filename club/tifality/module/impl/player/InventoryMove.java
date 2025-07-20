package club.tifality.module.impl.player;

import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.event.impl.packet.PacketSendEvent;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.property.Property;

@ModuleInfo(label = "InventoryMove", category = ModuleCategory.PLAYER)
public final class InventoryMove extends Module {

    private final Property<Boolean> cancelPacketProperty = new Property<>("Cancel Packet", false);
    public static final Property<Boolean> noMove = new Property<>("Cancel Inventory", false);

    @Listener
    public void onPacketSendEvent(PacketSendEvent event) {
        if (cancelPacketProperty.getValue() &&
                (event.getPacket() instanceof C16PacketClientStatus || event.getPacket() instanceof C0DPacketCloseWindow)) {
            event.setCancelled();
        }
    }
}
