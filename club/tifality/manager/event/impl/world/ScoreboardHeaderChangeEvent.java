package club.tifality.manager.event.impl.world;

import club.tifality.manager.event.Event;

public final class ScoreboardHeaderChangeEvent implements Event {
    private final String header;

    public ScoreboardHeaderChangeEvent(String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
    }
}
