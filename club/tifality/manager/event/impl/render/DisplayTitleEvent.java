package club.tifality.manager.event.impl.render;

import club.tifality.manager.event.CancellableEvent;

public final class DisplayTitleEvent extends CancellableEvent {

    private final String title;

    public DisplayTitleEvent(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
