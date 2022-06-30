package de.natrox.configuration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

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
        } else if (key.length == 1)
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
