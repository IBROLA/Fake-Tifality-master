package club.tifality.manager.api.bus;

public interface Bus<T> {
    void subscribe(final Object subscriber);

    void unsubscribe(final Object subscriber);

    void post(final T event);
}

