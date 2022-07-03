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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

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
        //return new Configuration(this.convertNode(in, null, ""));
        return convertNode(in, new Configuration());
    }

    private <C extends ConfigNode> C convertNode(JsonReader in, @NotNull C value) throws IOException {
        if (in.peek().equals(JsonToken.BEGIN_OBJECT)) {
            in.beginObject();
            String name;
            while (in.peek().equals(JsonToken.NAME)) {
                name = in.nextName();
                if ("value".equals(name))
                    value.set(gson.fromJson(in, Object.class));
                else {
                    ConfigNode subNode = new ConfigNode(name);
                    subNode.parent(value);
                    value.addNode(this.convertNode(in, subNode));
                }
            }
            in.endObject();
        } else
            value.set(gson.fromJson(in, Object.class));
        return value;
    }

    private ConfigNode convertNode(JsonReader in, @Nullable ConfigNode parentNode, @NotNull String id) throws IOException {
        ConfigNode value = new ConfigNode(id);
        value.parent(parentNode);
        if (in.peek().equals(JsonToken.BEGIN_OBJECT)) {
            in.beginObject();
            String name;
            while (in.peek().equals(JsonToken.NAME)) {
                name = in.nextName();
                if ("value".equals(name))
                    value.set(gson.fromJson(in, Object.class));
                else
                    value.addNode(this.convertNode(in, value, name));
            }
            in.endObject();
        } else
            value.set(gson.fromJson(in, Object.class));
        return value;
    }

    @Override
    public void write(JsonWriter out, Configuration value) {
        this.gson.toJson(this.convertNode(value), out);
    }

    private JsonObject convertNode(ConfigNode node) {
        JsonObject value = new JsonObject();
        if (node.hasValue())
            value.add("value", gson.toJsonTree(node.get()));
        for (ConfigNode subNode : node.subNodes().values()) {
            if ((!subNode.hasSubNodes()) && (!subNode.hasValue()))
                continue;
            if (subNode.hasSubNodes())
                value.add(subNode.id(), this.convertNode(subNode));
            else //hasValue, but not subCluster
                value.add(subNode.id(), gson.toJsonTree(subNode.get()));
        }
        return value;
    }
}
