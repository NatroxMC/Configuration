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
import de.natrox.configuration.exception.ConfigParseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.*;

public class ConfigNode {

    private final String id;
    private final Map<String, ConfigNode> subNodes;
    private ConfigNode parentNode;
    private Object value;

    public ConfigNode(@NotNull String id) {
        this.id = id;
        this.subNodes = new HashMap<>();
        this.parentNode = null;
    }

    public static <C extends ConfigNode> C copy(C source, C destination) {
        source.subNodes().values().forEach(node -> destination.addNode(node.copyTo(destination, node.id())));
        destination.set(source.get());
        return destination;
    }

    public void set(Object value) {
        this.value = value;
    }

    public @Nullable Object get() {
        return this.value;
    }

    public <V> @Nullable V get(Class<V> expected) {
        return expected.cast(this.value);
    }

    public @Nullable String getAsString() {
        if (this.value == null)
            return null;
        if (value instanceof String)
            return (String) value;
        return value.toString();
    }

    public @NotNull Number getAsNumber() {
        if (this.value == null)
            return 0;
        if (this.value instanceof Number)
            return (Number) this.value;
        try {
            return new BigDecimal(this.getAsString());
        } catch (NumberFormatException e) {
            throw new ConfigParseException("Value (" + value.getClass() + ") can not be converted to a Number.", e);
        }
    }

    public double getAsDouble() {
        return this.getAsNumber().doubleValue();
    }

    public long getAsLong() {
        return this.getAsNumber().longValue();
    }

    public float getAsFloat() {
        return this.getAsNumber().floatValue();
    }

    public int getAsInt() {
        return this.getAsNumber().intValue();
    }

    public short getAsShort() {
        return this.getAsNumber().shortValue();
    }

    public byte getAsByte() {
        return this.getAsNumber().byteValue();
    }

    public boolean getAsBoolean() {
        if (this.value instanceof Boolean)
            return (boolean) value;
        return Boolean.parseBoolean(this.getAsString());
    }

    public <V> V[] getAsArray(V[] copyTo) {
        if (this.value.getClass().isArray()) {
            System.arraycopy(this.value, 0, copyTo, 0, copyTo.length);
            return copyTo;
        } else try {
            return this.getAsList().toArray(copyTo);
        } catch (ConfigParseException e) {
            throw new ConfigParseException("Value (" + value.getClass() + ") can not be converted to Array.", e);
        }
    }

    public List<?> getAsList() {
        if (List.class.isAssignableFrom(this.value.getClass()))
            return (List<?>) this.value;
        if (Enumeration.class.isAssignableFrom(this.value.getClass()))
            return Collections.list((Enumeration<?>) this.value);
        if (Iterable.class.isAssignableFrom(this.value.getClass())) {
            List<Object> listValue = new ArrayList<>();
            for (Object o : (Iterable<?>) this.value)
                listValue.add(o);
            return listValue;
        }
        throw new ConfigParseException("Value (" + value.getClass() + ") can not be converted to java.util.List.");
    }

    public @NotNull String id() {
        return this.id;
    }

    public @NotNull ConfigNode node(String... path) {
        ConfigNode cursor = this;
        for (String s : path) {
            Check.stateCondition(s.contains("."), "Key must not contain '.' as it could cause problems.");
            Check.stateCondition("value".equals(s), "Key must not equal \"value\", as it could cause problems.");
            cursor = cursor.getNode(s);
        }
        return cursor;
    }

    public @NotNull Map<String, ConfigNode> subNodes() {
        return Collections.unmodifiableMap(this.subNodes);
    }

    public @Nullable ConfigNode parent() {
        return this.parentNode;
    }

    public void parent(ConfigNode newParent) {
        if (this.hasParent())
            this.parent().removeNode(this.id());
        this.parentNode = newParent;
    }

    private @NotNull ConfigNode getNode(String id) {
        if (!this.subNodes.containsKey(id))
            this.addNode(new ConfigNode(id));
        return this.subNodes.get(id);
    }

    public void addNodes(Iterable<ConfigNode> nodes) {
        nodes.forEach(this::addNode);
    }

    public void addNode(ConfigNode node) {
        if (this.hasSubNode(node.id()))
            this.removeNode(node.id());
        node.parent(this);
        this.subNodes.put(node.id(), node);
    }

    public void removeNode(String id) {
        if (this.hasSubNode(id))
            this.subNodes.remove(id).parent(null);
    }

    public boolean hasValue() {
        return this.value != null;
    }

    public boolean hasSubNodes() {
        return !this.subNodes.isEmpty();
    }

    public boolean hasSubNode(String id) {
        return this.subNodes.containsKey(id);
    }

    public boolean hasParent() {
        return this.parentNode != null;
    }

    public boolean isRoot() {
        return !this.hasParent();
    }

    private ConfigNode copyTo(ConfigNode parent, String id) {
        ConfigNode copy = this.copy(id);
        copy.parent(parent);
        return copy;
    }

    public ConfigNode copy(String id) {
        return ConfigNode.copy(this, new ConfigNode(id));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigNode that = (ConfigNode) o;
        return this.equalsNode(that);
    }

    public boolean equalsNode(ConfigNode that) {
        if (!Objects.equals(this.get(), that.get()))
            return false;
        for (ConfigNode thisSubNode : this.subNodes().values()) {
            if (!that.hasSubNode(thisSubNode.id()))
                return false;
            if (!thisSubNode.equalsNode(that.getNode(thisSubNode.id())))
                return false;
        }
        return true;
    }
}
