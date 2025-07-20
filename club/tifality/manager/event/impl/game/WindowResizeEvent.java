package club.tifality.manager.event.impl.game;

import club.tifality.manager.event.Event;
import net.minecraft.client.gui.ScaledResolution;

public final class WindowResizeEvent implements Event {

    private final ScaledResolution scaledResolution;

    public WindowResizeEvent(ScaledResolution scaledResolution) {
        this.scaledResolution = scaledResolution;
    }

    public ScaledResolution getScaledResolution() {
        return scaledResolution;
    }

}
