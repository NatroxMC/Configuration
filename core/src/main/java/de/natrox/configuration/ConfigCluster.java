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

import de.natrox.common.container.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConfigCluster {

    private final Map<String, ConfigCluster> subCluster;
    private Object value;

    ConfigCluster() {
        this.subCluster = new HashMap<>();
    }

    protected <V> void set(String[] key, V value, int depth) {
        if (key.length - depth > 0) {
            if ("*".equals(key[depth]))
                for (ConfigCluster subCluster : this.subCluster.values())
                    subCluster.set(key, value, depth + 1);
            else
                this.getCluster(key[depth]).set(key, value, depth + 1);
        } else
            this.value = value;
    }

    protected <V> @Nullable V get(String[] key, Class<V> expected, int depth) {
        if (key.length - depth == 0) {
            if (!this.hasValue())
                return null;
            return expected.cast(this.value());
        }
        return this.getCluster(key[depth]).get(key, expected, depth + 1);
    }

    protected Object value() {
        return this.value;
    }

    public @NotNull Map<String, ConfigCluster> subCluster() {
        return Collections.unmodifiableMap(this.subCluster);
    }

    private @NotNull ConfigCluster getCluster(String key) {
        if (!this.subCluster.containsKey(key))
            this.subCluster.put(key, new ConfigCluster());
        return this.subCluster.get(key);
    }

    void addClusterPaired(Iterable<? extends Pair<String, ConfigCluster>> childInfos) {
        for (Pair<String, ConfigCluster> childInfo : childInfos)
            addChild(childInfo.first(), childInfo.second());
    }

    void addClusterEntries(Iterable<? extends Map.Entry<String, ConfigCluster>> childInfos) {
        for (Map.Entry<String, ConfigCluster> childInfo : childInfos)
            addChild(childInfo.getKey(), childInfo.getValue());
    }

    void addChild(String localKey, ConfigCluster child) {
        this.subCluster.put(localKey, child);
    }

    void setValue(Object value) {
        this.value = value;
    }

    boolean hasValue() {
        return this.value != null;
    }

    boolean hasSubCluster() {
        return !this.subCluster.isEmpty();
    }
}
