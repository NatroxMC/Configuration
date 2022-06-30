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

    protected <V> @NotNull ConfigCluster add(String[] key, V value) {
        if(key.length > 1) {
            String[] dropped = new String[key.length - 1];
            System.arraycopy(key, 1, dropped, 0, key.length - 1);
            this.getCluster(key[0]).add(dropped, value);
        } else if(key.length == 1)
            properties.put(key[0], ConfigElement.of(value));
        return this;
    }

    protected @Nullable Object get(String[] key) {
        if(key.length == 1)
            return properties.get(key[0]).value();
        String[] dropped = new String[key.length - 1];
        System.arraycopy(key, 1, dropped, 0, key.length - 1);
        return this.getCluster(key[0]).get(dropped);
    }

    protected <V> @Nullable V getStrict(String[] key, Class<V> expected) {
        if(key.length == 1)
            return expected.cast(properties.get(key[0]).value());
        String[] dropped = new String[key.length - 1];
        System.arraycopy(key, 1, dropped, 0, key.length - 1);
        return this.getCluster(key[0]).getStrict(dropped, expected);
    }

    public @NotNull Map<String, ConfigElement<?>> properties() {
        return Collections.unmodifiableMap(this.properties);
    }

    public @NotNull Map<String, ConfigCluster> children() {
        return Collections.unmodifiableMap(this.children);
    }

    private @NotNull ConfigCluster getCluster(String key) {
        if(!children.containsKey(key))
            children.put(key, new ConfigCluster());
        return children.get(key);
    }

    @NotNull ConfigCluster addCluster(String key, ConfigCluster cluster) {
        //assert key does not contain "."
        children.put(key, cluster);
        return this;
    }

    @NotNull ConfigCluster addProperty(String key, ConfigElement<?> property) {
        //assert key does not contain "."
        properties.put(key, property);
        return this;
    }

    public @Nullable Object get(String key) {
        if(!properties.containsKey(key))
            return null;
        return properties.get(key).value();
    }
}
