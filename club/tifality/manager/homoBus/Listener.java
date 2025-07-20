package club.tifality.manager.homoBus;

@FunctionalInterface
public interface Listener<Event> {
    void call(Event event);
}