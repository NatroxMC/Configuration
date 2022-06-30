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
