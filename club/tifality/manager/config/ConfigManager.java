package club.tifality.manager.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FilenameUtils;
import club.tifality.utils.handler.Manager;

import java.io.*;
import java.util.ArrayList;

public final class ConfigManager extends Manager<Config> {

    public ConfigManager() {
        super(loadConfigs());

        if (!CONFIGS_DIR.exists()) {
            boolean ignored = CONFIGS_DIR.mkdirs();
        }
    }

    public static final File CONFIGS_DIR = new File("LitelyWare", "configs");
    public static final String EXTENSION = ".json";
    public static ArrayList<CustomFile> Files = new ArrayList<>();

    public boolean loadConfig(String configName) {
        if (configName == null) return false;
        Config config = findConfig(configName);

        if (config == null) return false;
        try {
            FileReader reader = new FileReader(config.getFile());
            JsonParser parser = new JsonParser();
            JsonObject object = (JsonObject) parser.parse(reader);
            config.load(object);
            return true;
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    public boolean saveConfig(String configName) {
        if (configName == null) return false;
        Config config;
        if ((config = findConfig(configName)) == null) {
            Config newConfig = (config = new Config(configName));
            getElements().add(newConfig);
        }

        String contentPrettyPrint = new GsonBuilder().setPrettyPrinting().create().toJson(config.save());
        try {
            FileWriter writer = new FileWriter(config.getFile());
            writer.write(contentPrettyPrint);
            writer.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public Config findConfig(String configName) {
        if (configName == null) return null;
        for (Config config : getElements()) {
            if (config.getName().equalsIgnoreCase(configName))
                return config;
        }

        if (new File(CONFIGS_DIR, configName + EXTENSION).exists())
            return new Config(configName);

        return null;
    }

    public boolean deleteConfig(String configName) {
        if (configName == null) return false;
        Config config;
        if ((config = findConfig(configName)) != null) {
            final File f = config.getFile();
            getElements().remove(config);
            return f.exists() && f.delete();
        }
        return false;
    }

    private static ArrayList<Config> loadConfigs() {
        final ArrayList<Config> loadedConfigs = new ArrayList<>();
        File[] files = CONFIGS_DIR.listFiles();
        if (files != null) {
            for (File file : files) {
                if (FilenameUtils.getExtension(file.getName()).equals("json"))
                    loadedConfigs.add(new Config(FilenameUtils.removeExtension(file.getName())));
            }
        }
        return loadedConfigs;
    }

    public void loadFiles() {
        for (CustomFile file : Files) {
            try {
                if (!file.loadOnStart()) continue;
                file.loadFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void saveFiles() {
        for (CustomFile file : Files) {
            try {
                file.saveFile();
                System.out.println("SaveFiles");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public CustomFile getFile(Class<?> clazz) {
        for (CustomFile file : Files) {
            if (file.getClass() == clazz) {
                return file;
            }
        }
        return null;
    }

    public static abstract class CustomFile {
        private final File file;
        private final String name;
        private final boolean load;

        public CustomFile(String name, boolean loadOnStart) {
            this.name = name;
            this.load = loadOnStart;
            this.file = new File("LitelyWare", name + ".txt");
            if (!this.file.exists()) {
                try {
                    this.saveFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public final File getFile() {
            return this.file;
        }

        private boolean loadOnStart() {
            return this.load;
        }

        public final String getName() {
            return this.name;
        }

        public abstract void loadFile() throws IOException;

        public abstract void saveFile() throws IOException;
    }
}
