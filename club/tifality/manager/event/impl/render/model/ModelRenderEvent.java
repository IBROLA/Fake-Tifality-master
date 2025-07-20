package club.tifality.manager.event.impl.render.model;

import club.tifality.manager.event.Event;

public final class ModelRenderEvent implements Event {

    private ModelRenderState state;

    public ModelRenderEvent(ModelRenderState state) {
        this.state = state;
    }

    public ModelRenderState getState() {
        return state;
    }

    public void setState(ModelRenderState state) {
        this.state = state;
    }

}
