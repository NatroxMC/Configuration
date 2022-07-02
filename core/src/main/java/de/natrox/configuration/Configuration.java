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

import de.natrox.common.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Configuration extends ConfigCluster {

    Configuration(ConfigCluster cluster) {
        this.addClusterEntries(cluster.subCluster().entrySet());
    }

    public Configuration() {
        super();
    }

    public <V> @NotNull Configuration set(@NotNull String key, @Nullable V value) {
        this.validateKey(key);
        super.set(key.split("\\."), value, 0);
        return this;
    }

    public <V> @Nullable V get(@NotNull String key, @NotNull Class<V> expected) {
        Check.notNull(expected, "Expected class must not be null.");
        Check.stateCondition(key.contains("*"), "'*' expressions forbidden in getters.");
        return super.get(this.validateKey(key), expected, 0);
    }

    public <V> @Nullable V get(@NotNull String key) {
        Check.stateCondition(key.contains("*"), "'*' expressions forbidden in getters.");
        return super.get(this.validateKey(key), 0);
    }

    private String[] validateKey(String key) {
        Check.notNull(key, "Key must not be null.");
        Check.stateCondition("".equals(key), "Key must not be empty.");
        Check.stateCondition(key.contains("..") || key.startsWith("."), "Cluster key must not be empty.");
        Check.stateCondition(key.endsWith("."), "Property key must not be empty.");
        String[] keys = key.split("\\.");
        for (String singleKey : keys)
            this.validateSingleKey(singleKey);
        return keys;
    }

    private void validateSingleKey(String singleKey) {
        if (!"*".equals(singleKey))
            Check.stateCondition(singleKey.contains("*"), "Key must not contain '*'.");
        Check.stateCondition(singleKey.equals("children"), "Key must not equal \"children\", as this expression could cause problems.");
        Check.stateCondition(singleKey.equals("value"), "Key must not equal \"value\", as this expression could cause problems.");
    }
}
