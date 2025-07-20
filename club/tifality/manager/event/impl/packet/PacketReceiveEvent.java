package club.tifality.manager.event.impl.packet;

import club.tifality.manager.event.CancellableEvent;
import net.minecraft.network.Packet;

public final class PacketReceiveEvent extends CancellableEvent {

    private Packet<?> packet;

    public PacketReceiveEvent(Packet<?> packet) {
        this.packet = packet;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }

}
