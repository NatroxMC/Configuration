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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConfigCluster {

    private final Map<String, ConfigElement<?>> properties;
    private final Map<String, ConfigCluster> children;

    ConfigCluster() {
        this.properties = new HashMap<>();
        this.children = new HashMap<>();
    }

    protected <V> void add(String[] key, V value, int depth) {
        if(key.length - depth > 1)
            this.getCluster(key[depth]).add(key, value, depth + 1);
        else if(key.length - depth == 1)
            this.properties.put(key[depth], ConfigElement.of(value));
        else
            throw new IllegalStateException("Depth equals zero or is lower.");
    }

    protected <V> @Nullable V get(String[] key, Class<V> expected, int depth) {
        if(key.length - depth == 1) {
            if(!this.properties.containsKey(key[depth]))
                return null;
            return expected.cast(this.properties.get(key[depth]).value());
        }
        return this.getCluster(key[depth]).get(key, expected, depth + 1);
    }

    public @NotNull Map<String, ConfigElement<?>> properties() {
        return Collections.unmodifiableMap(this.properties);
    }

    public @NotNull Map<String, ConfigCluster> children() {
        return Collections.unmodifiableMap(this.children);
    }

    private @NotNull ConfigCluster getCluster(String key) {
        if(!this.children.containsKey(key))
            this.children.put(key, new ConfigCluster());
        return this.children.get(key);
    }

    @NotNull void addCluster(String localKey, ConfigCluster cluster) {
        this.children.put(localKey, cluster);
    }

    @NotNull void addProperty(String localKey, ConfigElement<?> property) {
        this.properties.put(localKey, property);
    }
}
