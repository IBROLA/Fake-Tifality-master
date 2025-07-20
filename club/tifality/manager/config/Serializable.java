package club.tifality.manager.config;

import com.google.gson.JsonObject;

public interface Serializable {

    JsonObject save();

    void load(JsonObject object);

}
