package club.tifality.manager.config;

import club.tifality.Tifality;
import com.google.gson.JsonObject;
import club.tifality.gui.csgo.SkeetUI;
import club.tifality.module.Module;

import java.io.File;
import java.io.IOException;

public final class Config implements Serializable {

    private final String name;
    private final File file;

    public Config(String name) {
        this.name = name;
        this.file = new File(ConfigManager.CONFIGS_DIR, name + ConfigManager.EXTENSION);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ignored) {
            }
        }
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    @Override
    public JsonObject save() {
        JsonObject jsonObject = new JsonObject();
        JsonObject modulesObject = new JsonObject();
        JsonObject guiColorObject = new JsonObject();

        for (Module module : Tifality.getInstance().getModuleManager().getModules())
            modulesObject.add(module.getLabel(), module.save());

        guiColorObject.addProperty(SkeetUI.colorProperty.getLabel(), Integer.toHexString(SkeetUI.getColor()));

        jsonObject.add("Modules", modulesObject);
        jsonObject.add("GUI", guiColorObject);
        return jsonObject;
    }

    @Override
    public void load(JsonObject object) {
        if (object.has("Modules")) {
            JsonObject modulesObject = object.getAsJsonObject("Modules");

            for (Module module : Tifality.getInstance().getModuleManager().getModules()) {
                if (modulesObject.has(module.getLabel()))
                    module.load(modulesObject.getAsJsonObject(module.getLabel()));
            }
        }
        if (object.has("GUI")) {
            JsonObject guiColorObject = object.getAsJsonObject("GUI");
            SkeetUI.colorProperty.setValue((int) Long.parseLong(guiColorObject.get(SkeetUI.colorProperty.getLabel()).getAsString(), 16));
        }
    }
}
