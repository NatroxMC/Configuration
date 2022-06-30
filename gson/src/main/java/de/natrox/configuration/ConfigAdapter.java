package de.natrox.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConfigAdapter extends TypeAdapter<Config> {

    private final Gson gson = new GsonBuilder().create();

    @Override
    public void write(JsonWriter out, Config value) throws IOException {
        gson.toJson(this.convertCluster(value), out);
    }

    @Override
    public Config read(JsonReader in) throws IOException {
        return this.convertCluster(in);
    }

    private Config convertCluster(JsonReader in) throws IOException {
        Config value = new Config();
        in.beginObject();
        while(in.peek() == JsonToken.NAME) {
            String name = in.nextName();
            if("children".equals(name))
                for(Map.Entry<String, ConfigCluster> child : this.convertChildren(in))
                    value.addCluster(child.getKey(), child.getValue());
            else if("properties".equals(name))
                for(Map.Entry<String, ConfigElement<?>> child : this.convertProperties(in))
                    value.addProperty(child.getKey(), child.getValue());
            else
                throw new RuntimeException();
        }
        in.endObject();
        return value;
    }

    private Set<Map.Entry<String, ConfigCluster>> convertChildren(JsonReader in) throws IOException {
        Set<Map.Entry<String, ConfigCluster>> value = new HashSet<>();
        in.beginObject();
        while(in.peek() == JsonToken.NAME)
            value.add(Map.entry(in.nextName(), this.convertCluster(in)));
        in.endObject();
        return value;
    }

    private Set<Map.Entry<String, ConfigElement<?>>> convertProperties(JsonReader in) throws IOException {
        Set<Map.Entry<String, ConfigElement<?>>> value = new HashSet<>();
        in.beginObject();
        while (in.peek() != JsonToken.END_OBJECT)
            value.add(Map.entry(in.nextName(), this.convertElement(in)));
        in.endObject();
        return value;
    }

    private ConfigElement<?> convertElement(JsonReader in) throws IOException {
        if(in.peek().equals(JsonToken.STRING))
            return ConfigElement.of(in.nextString());
        return ConfigElement.of(gson.fromJson(in, Object.class));
    }

    private JsonObject convertCluster(ConfigCluster cluster) {
        JsonObject value = new JsonObject();
        value.add("children", this.convertChildren(cluster.children().entrySet()));
        value.add("properties", this.convertProperties(cluster.properties().entrySet()));
        return value;
    }

    private JsonObject convertChildren(Set<Map.Entry<String, ConfigCluster>> entrySet) {
        JsonObject value = new JsonObject();
        for(Map.Entry<String, ConfigCluster> entry : entrySet)
            value.add(entry.getKey(), this.convertCluster(entry.getValue()));
        return value;
    }

    private JsonObject convertProperties(Set<Map.Entry<String, ConfigElement<?>>> entrySet) {
        JsonObject value = new JsonObject();
        for(Map.Entry<String, ConfigElement<?>> entry : entrySet)
            value.add(entry.getKey(), gson.toJsonTree(entry.getValue().value()));
        return value;
    }
}
