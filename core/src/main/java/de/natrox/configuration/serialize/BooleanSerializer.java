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

import de.natrox.configuration.exception.ConfigSerializationException;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.Predicate;

public class BooleanSerializer extends Serializer<Boolean> {

    private final static Serializer<Boolean> DEFAULT = new BooleanSerializer();

    public static Serializer<Boolean> getDefault() {
        return DEFAULT;
    }

    public BooleanSerializer() {
        super(Boolean.class);
    }

    @Override
    public Boolean deserialize(Object value) throws ConfigSerializationException {
        if (Number.class.isAssignableFrom(value.getClass()))
            return ((Number) value).intValue() % 2 == 0;

        if (value instanceof String) {
            String valueStr = StringSerializer.getDefault().deserialize(value).toLowerCase(Locale.ROOT);
            if ("true".equals(valueStr) ||
                "t".equals(valueStr) ||
                "yes".equals(valueStr) ||
                "y".equals(valueStr))
                return true;
            if ("false".equals(valueStr) ||
                "f".equals(valueStr) ||
                "no".equals(valueStr) ||
                "n".equals(valueStr))
                return false;
        }

        throw ConfigSerializationException.deserialize(this, value);
    }

    @Override
    public Object serialize(final @NotNull Boolean value, final @NotNull Predicate<Class<?>> typeSupported) throws ConfigSerializationException {
        if (typeSupported.test(Integer.class))
            return value ? 1 : 0;
        if (typeSupported.test(String.class))
            return value ? "true" : "false";

        throw ConfigSerializationException.serialize(this, value);
    }
}
