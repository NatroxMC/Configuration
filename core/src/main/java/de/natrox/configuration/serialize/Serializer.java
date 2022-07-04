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

package de.natrox.configuration.serialize;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public abstract class Serializer<T> {

    private final Class<T> type;

    public Serializer(Class<T> type) {
        this.type = type;
    }

    public Class<T> type() {
        return type;
    }

    public abstract T deserialize(final Object value);

    @SuppressWarnings("unchecked")
    public T deserializeSave(final Object value) {
        if (type.isAssignableFrom(value.getClass()))
            return (T) value;
        return deserialize(value);
    }

    public abstract Object serialize(final @NotNull T value, final @NotNull Predicate<Class<?>> typeSupported);
}

