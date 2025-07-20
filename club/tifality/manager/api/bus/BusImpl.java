package club.tifality.manager.api.bus;

import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.api.annotations.Priority;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BusImpl<T> implements Bus<T> {
    private static final Site[] PLACEHOLDER = new Site[1];
    private final Map<Class<?>, List<Site>> map = new HashMap<>();

    @Override
    public void subscribe(Object subscriber) {
        for (Method method : subscriber.getClass().getDeclaredMethods()) {
            Class<?>[] params;
            Listener listener = method.getAnnotation(Listener.class);
            if (listener == null || (params = method.getParameterTypes()).length != 1) continue;
            Map<Class<?>, List<Site>> map2 = this.map;
            Class<?> ecs = params[0];
            Site cl = new Site(subscriber, method, listener.value());
            if (map2.containsKey(ecs)) {
                List<Site> ss = map2.get(ecs);
                ss.add(cl);
                ss.sort(Comparator.comparingInt(site -> site.priority));
                continue;
            }
            BusImpl.PLACEHOLDER[0] = cl;
            map2.put(ecs, new ArrayList<>(Arrays.asList(PLACEHOLDER)));
        }
    }

    @Override
    public void unsubscribe(Object subscriber) {
        for (List<Site> classes : this.map.values()) {
            classes.removeIf(site -> site.site == subscriber);
            classes.sort(Comparator.comparingInt(site -> site.priority));
        }
    }

    @Override
    public void post(T event) {
        List<Site> clazz = this.map.get(event.getClass());
        try {
            if (clazz != null && !clazz.isEmpty()) {
                for (Site site : clazz) {
                    try {
                        Method method = site.method;
                        Object sub = site.site;
                        method.invoke(sub, event);
                    }
                    catch (IllegalAccessException | IndexOutOfBoundsException | InvocationTargetException ignored) {}
                }
            }
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("exception 123");
        }
    }

    static final class Site {
        final Object site;
        final Method method;
        final byte priority;

        Site(Object site, Method method, Priority priority) {
            this.site = site;
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            this.method = method;
            this.priority = (byte) priority.ordinal();
        }
    }
}

