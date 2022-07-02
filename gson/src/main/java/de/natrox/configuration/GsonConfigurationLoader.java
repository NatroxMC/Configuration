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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import org.jetbrains.annotations.UnknownNullability;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GsonConfigurationLoader implements ConfigurationLoader {

    private static final TypeAdapter<Configuration> DEFAULT_ADAPTER = new ConfigAdapter();

    private final Path configPath;
    private final Gson gson;

    public GsonConfigurationLoader(final Path configPath) {
        this.configPath = configPath;
        this.gson = new GsonBuilder().registerTypeAdapter(Configuration.class, new ConfigAdapter()).setPrettyPrinting().serializeNulls().create();
    }

    public GsonConfigurationLoader(final Path configPath, final TypeAdapter<Configuration> adapter) {
        this.configPath = configPath;
        this.gson = new GsonBuilder().registerTypeAdapter(Configuration.class, adapter).setPrettyPrinting().serializeNulls().create();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Configuration load() throws IOException {
        JsonReader reader = new JsonReader(new FileReader(configPath.toFile()));
        return gson.fromJson(reader, Configuration.class);
    }

    @Override
    public void save(Configuration config) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(configPath);
        gson.toJson(config, writer);
        writer.flush();
        writer.close();
    }

    public final static class Builder implements ConfigurationLoader.Builder<GsonConfigurationLoader> {

        private Path configPath;
        private TypeAdapter<Configuration> adapter = DEFAULT_ADAPTER;

        public Builder path(final Path configPath) {
            this.configPath = configPath;
            return this;
        }

        public Builder adapter(final TypeAdapter<Configuration> adapter) {
            this.adapter = adapter;
            return this;
        }

        @Override
        public @UnknownNullability GsonConfigurationLoader build() {
            return new GsonConfigurationLoader(this.configPath, adapter);
        }
    }
}
