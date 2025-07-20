package club.tifality.manager.event.impl.packet;

import club.tifality.manager.event.Event;

public final class DisconnectEvent implements Event {

    private final String reason;

    public DisconnectEvent(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
