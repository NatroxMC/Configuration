/*
 * Copyright 2020-2022 NatroxMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.natrox.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import de.natrox.common.container.Pair;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConfigAdapter extends TypeAdapter<Configuration> {

    private static final Gson DEFAULT_GSON = new GsonBuilder().serializeNulls().create();
    private final Gson gson;

    public ConfigAdapter() {
        this.gson = DEFAULT_GSON;
    }

    public ConfigAdapter(Gson gson) {
        this.gson = gson;
    }

    @Override
    public Configuration read(JsonReader in) throws IOException {
        return new Configuration(this.convertCluster(in));
    }

    private ConfigCluster convertCluster(JsonReader in) throws IOException {
        ConfigCluster value = new ConfigCluster();
        if (in.peek().equals(JsonToken.BEGIN_OBJECT)) {
            in.beginObject();
            String name;
            while (in.peek().equals(JsonToken.NAME)) {
                name = in.nextName();
                if ("value".equals(name))
                    value.setValue(gson.fromJson(in, Object.class));
                else
                    value.addChild(name, convertCluster(in));
            }
            in.endObject();
        } else
            value.setValue(gson.fromJson(in, Object.class));
        return value;
    }

    private Iterable<? extends Pair<String, ConfigCluster>> convertChildren(JsonReader in) throws IOException {
        Set<Pair<String, ConfigCluster>> value = new HashSet<>();
        in.beginObject();
        while (in.peek() == JsonToken.NAME)
            value.add(Pair.of(in.nextName(), this.convertCluster(in)));
        in.endObject();
        return value;
    }

    @Override
    public void write(JsonWriter out, Configuration value) {
        this.gson.toJson(this.convertCluster(value), out);
    }

    private JsonObject convertCluster(ConfigCluster cluster) {
        JsonObject value = new JsonObject();
        if (cluster.hasValue())
            value.add("value", gson.toJsonTree(cluster.value()));
        for (Map.Entry<String, ConfigCluster> childInfo : cluster.subCluster().entrySet()) {
            if (childInfo.getValue().hasSubCluster())
                value.add(childInfo.getKey(), convertCluster(childInfo.getValue()));
            else
                value.add(childInfo.getKey(), gson.toJsonTree(childInfo.getValue().value()));
        }
        return value;
    }
}
