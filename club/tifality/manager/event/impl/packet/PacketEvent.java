package club.tifality.manager.event.impl.packet;

import club.tifality.manager.event.CancellableEvent;
import club.tifality.manager.event.Event;
import net.minecraft.network.Packet;

public class PacketEvent extends CancellableEvent implements Event {
    private final State state;
    private final Packet<?> packet;

    public PacketEvent(Packet<?> packet, State state) {
        this.state = state;
        this.packet = packet;
    }

    public State getState() {
        return state;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    public boolean isOutgoing() {
        return state.equals(PacketEvent.State.INCOMING);
    }


    public enum State {
        INCOMING,
        OUTGOING
    }
}
