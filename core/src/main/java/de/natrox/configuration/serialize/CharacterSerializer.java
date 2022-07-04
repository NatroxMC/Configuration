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

public class CharacterSerializer extends Serializer<Character> {

    private final static Serializer<Character> DEFAULT = new CharacterSerializer();

    public static Serializer<Character> getDefault() {
        return DEFAULT;
    }

    public CharacterSerializer() {
        super(Character.class);
    }

    @Override
    public Character deserialize(Object value) {
        if (Number.class.isAssignableFrom(value.getClass()))
            return (char) ((Number) value).shortValue();

        if (value instanceof String) {
            String valueStr = StringSerializer.getDefault().deserialize(value).toLowerCase(Locale.ROOT);
            if (valueStr.length() == 1)
                return valueStr.charAt(0);
        }

        throw ConfigSerializationException.deserialize(this, value);
    }

    @Override
    public Object serialize(@NotNull Character value, @NotNull Predicate<Class<?>> typeSupported) {
        return null;
    }
}
