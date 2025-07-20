package club.tifality.gui.altmanager.althening.api.utilities;

import java.lang.reflect.Field;

public class ReflectionUtility {
    private String className;
    private Class<?> clazz;

    public ReflectionUtility(String className) {
        try {
            this.clazz = Class.forName(className);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public void setStaticField(String className, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field clazz = this.clazz.getDeclaredField(className);
        clazz.setAccessible(true);
        Field field = Field.class.getDeclaredField("modifiers");
        field.setAccessible(true);
        field.setInt(clazz, clazz.getModifiers() & 0xFFFFFFEF);
        clazz.set(null, value);
    }
}

