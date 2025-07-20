package club.tifality.manager.event.impl.world;

import club.tifality.manager.event.Event;

public final class ScoreboardModeChangeEvent implements Event {

    private final String mode;

    public ScoreboardModeChangeEvent(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }

}
