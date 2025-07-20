package club.tifality.manager.keybind;

import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.event.impl.KeyPressEvent;
import club.tifality.module.Module;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class BindSystem {
    private final Set<Bindable> objects;

    public BindSystem(Collection<Module> modules) {
        this.objects = new HashSet<>(modules);
    }

    public void register(Bindable object) {
        this.objects.add(object);
    }
    
    @Listener
    public void onKeyPress(KeyPressEvent event) {
        for (Bindable object : this.objects) {
            if (object.getKey() == event.getKey()) {
                object.onPress();
            }
        }
    }
}
