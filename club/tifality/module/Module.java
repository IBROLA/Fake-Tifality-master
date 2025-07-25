package club.tifality.module;

import club.tifality.Tifality;
import club.tifality.manager.config.Serializable;
import club.tifality.manager.keybind.Bindable;
import club.tifality.property.Property;
import club.tifality.property.impl.DoubleProperty;
import club.tifality.property.impl.EnumProperty;
import club.tifality.property.impl.MultiSelectEnumProperty;
import club.tifality.utils.StringUtils;
import club.tifality.utils.handler.Manager;
import club.tifality.utils.render.Translate;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.Minecraft;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Module extends Manager<Property<?>> implements Toggleable, Serializable, Bindable {

    private final String label = getClass().getAnnotation(ModuleInfo.class).label();
    private final String description = getClass().getAnnotation(ModuleInfo.class).description();
    private final ModuleCategory category = getClass().getAnnotation(ModuleInfo.class).category();
    private final Translate translate = new Translate(0.0, 0.0);
    private int key = getClass().getAnnotation(ModuleInfo.class).key();
    private boolean enabled;
    private boolean hidden;
    private Supplier<String> suffix;
    private String updatedSuffix;
    public static final Minecraft mc = Minecraft.getMinecraft();

    public String getUpdatedSuffix() {
        return updatedSuffix;
    }

    public void setUpdatedSuffix(String updatedSuffix) {
        this.updatedSuffix = updatedSuffix;
    }

    private void updateSuffix(EnumProperty<?> mode) {
        setUpdatedSuffix(StringUtils.upperSnakeCaseToPascal(mode.getValue().name()));
    }

    public void setSuffixListener(EnumProperty<?> mode) {
        updateSuffix(mode);
        mode.addValueChangeListener((oldValue, value) -> updateSuffix(mode));
    }

    public void resetPropertyValues() {
        for (Property<?> property : getElements())
            property.callFirstTime();
    }

    public Translate getTranslate() {
        return translate;
    }

    public Supplier<String> getSuffix() {
        return suffix;
    }

    public void setSuffix(Supplier<String> suffix) {
        this.suffix = suffix;
    }

    public ModuleCategory getCategory() {
        return category;
    }

    public void reflectProperties() {
        for (final Field field : getClass().getDeclaredFields()) {
            final Class<?> type = field.getType();
            if (type.isAssignableFrom(Property.class) ||
                    type.isAssignableFrom(DoubleProperty.class) ||
                    type.isAssignableFrom(EnumProperty.class) ||
                    type.isAssignableFrom(MultiSelectEnumProperty.class)) {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                try {
                    elements.add((Property<?>) field.get(this));
                } catch (IllegalAccessException ignored) {
                }
            }
        }
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getDescription() {
        return description;
    }

    public String getLabel() {
        return label;
    }

    public String getDisplayLabel() {
        return this.suffix == null && this.updatedSuffix == null
                ? this.label
                : this.label + " §7" + (this.updatedSuffix != null ? this.updatedSuffix : this.suffix.get());
    }


    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    @Override
    public void onPress() {
        this.toggle();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;

            if (enabled) {
                onEnable();
                Tifality.getInstance().getEventBus().subscribe(this);
            } else {
                Tifality.getInstance().getEventBus().unsubscribe(this);
                onDisable();
            }
        }
    }

    public boolean isVisible() {
        return enabled && !hidden;
    }

    @Override
    public void toggle() {
        setEnabled(!enabled);
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    @Override
    public JsonObject save() {
        JsonObject object = new JsonObject();
        object.addProperty("toggled", isEnabled());
        object.addProperty("key", getKey());
        object.addProperty("hidden", isHidden());
        List<Property<?>> properties = getElements();
        if (!properties.isEmpty()) {
            JsonObject propertiesObject = new JsonObject();

            for (Property<?> property : properties) {
                if (property instanceof DoubleProperty) {
                    propertiesObject.addProperty(property.getLabel(), ((DoubleProperty) property).getValue());
                } else if (property instanceof EnumProperty) {
                    EnumProperty<?> enumProperty = (EnumProperty<?>) property;
                    propertiesObject.add(property.getLabel(), new JsonPrimitive(enumProperty.getValue().name()));
                } else if (property instanceof MultiSelectEnumProperty) {
                    MultiSelectEnumProperty<?> multiSelect = (MultiSelectEnumProperty<?>) property;
                    List<?> selectedValues = multiSelect.getValue();

                    JsonArray jsonArray = new JsonArray();
                    for (Object value : selectedValues) {
                        jsonArray.add(new JsonPrimitive(value.toString()));
                    }

                    propertiesObject.add(property.getLabel(), jsonArray);
                } else if (property.getType() == Boolean.class) {
                    propertiesObject.addProperty(property.getLabel(), (Boolean) property.getValue());
                } else if (property.getType() == Integer.class) {
                    propertiesObject.addProperty(property.getLabel(), Integer.toHexString((Integer) property.getValue()));
                } else if (property.getType() == String.class) {
                    propertiesObject.addProperty(property.getLabel(), (String) property.getValue());
                }
            }

            object.add("Properties", propertiesObject);
        }
        return object;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void load(JsonObject object) {
        if (object.has("toggled"))
            setEnabled(object.get("toggled").getAsBoolean());

        if (object.has("key"))
            setKey(object.get("key").getAsInt());

        if (object.has("hidden"))
            setHidden(object.get("hidden").getAsBoolean());

        if (object.has("Properties") && !getElements().isEmpty()) {
            JsonObject propertiesObject = object.getAsJsonObject("Properties");
            for (Property<?> property : getElements()) {
                if (propertiesObject.has(property.getLabel())) {
                    if (property instanceof DoubleProperty) {
                        ((DoubleProperty) property).setValue(propertiesObject.get(property.getLabel()).getAsDouble());
                    } else if (property instanceof EnumProperty) {
                        findEnumValue(property, propertiesObject);
                    } else if (property instanceof MultiSelectEnumProperty) {
                        findMultiSelectProperty(property, propertiesObject);
                    } else if (property.getValue() instanceof Boolean) {
                        ((Property<Boolean>) property).setValue(propertiesObject.get(property.getLabel()).getAsBoolean());
                    } else if (property.getValue() instanceof Integer) {
                        ((Property<Integer>) property).setValue((int) Long.parseLong(propertiesObject.get(property.getLabel()).getAsString(), 16));
                    } else if (property.getValue() instanceof String) {
                        ((Property<String>) property).setValue(propertiesObject.get(property.getLabel()).getAsString());
                    }
                }
            }
        }
    }

    private static <T extends Enum<T>> void findEnumValue(Property<?> property, JsonObject propertiesObject) {
        EnumProperty<T> enumProperty = (EnumProperty<T>) property;
        String value = propertiesObject.getAsJsonPrimitive(property.getLabel()).getAsString();
        for (T possibleValue : enumProperty.getValues()) {
            if (possibleValue.name().equalsIgnoreCase(value)) {
                enumProperty.setValue(possibleValue);
                break;
            }
        }
    }

    private static <T extends Enum<T>> void findMultiSelectProperty(Property<?> property, JsonObject propertiesObject) {
        MultiSelectEnumProperty<T> multiSelectProperty = (MultiSelectEnumProperty<T>) property;
        JsonArray jsonArray = propertiesObject.getAsJsonArray(property.getLabel());
        List<T> selectedValues = new ArrayList<>();

        for (JsonElement element : jsonArray) {
            String enumName = element.getAsString();
            for (T possibleValue : multiSelectProperty.getValues()) {
                if (possibleValue.name().equalsIgnoreCase(enumName)) {
                    selectedValues.add(possibleValue);
                    break;
                }
            }
        }

        multiSelectProperty.setValue(selectedValues);
    }
}
