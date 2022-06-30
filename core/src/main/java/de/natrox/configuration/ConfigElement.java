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

public class ConfigElement<V> {

    private V value;

    private ConfigElement(V value) {
        this.value = value;
    }

    static <V> ConfigElement<V> of(V value) {
        return new ConfigElement<>(value);
    }

    public V value() {
        return this.value;
    }

    public V value(V value) {
        this.value = value;
        return this.value;
    }
}
