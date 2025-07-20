package club.tifality.property;

@FunctionalInterface
public interface ValueChangeListener<T> {

    void onValueChange(T oldValue, T value);

}
