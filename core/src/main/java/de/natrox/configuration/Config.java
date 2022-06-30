package de.natrox.configuration;

import org.jetbrains.annotations.NotNull;

public class Config extends ConfigCluster {

    public <V> @NotNull Config add(String key, V value) {
        super.add(key.split("\\."), value);
        return this;
    }
}
