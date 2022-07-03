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

import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

//This file will be rewritten later.
class ConfigurationLoaderTest {

    private static final boolean keepTestFiles = Boolean.FALSE;
    private static GsonConfigurationLoader loader;
    private static File testFile;

    @BeforeAll
    private static void init() {
        if (keepTestFiles)
            return;
        testFile = generateValidTestFile();
        loader = generateLoader();
    }

    private static GsonConfigurationLoader generateLoader() {
        return GsonConfigurationLoader
            .builder()
            .adapter(new ConfigAdapter(new GsonBuilder()
                .serializeNulls()
                .create()))
            .path(testFile.toPath())
            .build();
    }

    private static File generateValidTestFile() {
        String filename = "Test-" + UUID.randomUUID().toString().substring(0, 8) + ".json";
        File file = new File(filename);
        while (file.exists())
            file = new File(filename);
        return file;
    }

    @AfterAll
    private static void clear() throws IOException {
        if (keepTestFiles)
            return;
        System.gc();
        try {
            Files.delete(testFile.toPath());
        } catch (Exception e) {
            System.err.println("File for testing could not be deleted. Please do it manually. (" + testFile.getAbsolutePath() + ")");
            throw e;
        }
    }

    private static Configuration generateNumberedConfig() {
        Configuration numberedConfig = new Configuration();
        numberedConfig.node("number", "zero").set(0d);
        numberedConfig.node("number", "one").set(1d);
        numberedConfig.node("text", "zero").set("zero");
        numberedConfig.node("text", "one").set("one");
        return numberedConfig;
    }

    private static void checkNumberedConfig(Configuration numberedConfig) {
        assertEquals(0d, numberedConfig.node("number", "zero").getAsDouble());
        assertEquals(1d, numberedConfig.node("number", "one").getAsDouble());
        assertEquals("zero", numberedConfig.node("text", "zero").getAsString());
        assertEquals("one", numberedConfig.node("text", "one").getAsString());
    }

    @BeforeEach
    private void initEach() {
        if (!keepTestFiles)
            return;
        testFile = generateValidTestFile();
        loader = generateLoader();
    }

    @Test
    void navigationTest() {
        Configuration config = new Configuration();
        ConfigNode aNode = config.node("a");
        ConfigNode bNode = config.node("b");
        assertEquals(aNode.parent(), bNode.parent(), "Both nodes should have the same parent (config, root-node, id \"\")");
        assertEquals(config, aNode.parent(), "Both nodes should have the same parent (config, root-node, id \"\")");
    }

    @Test
    void storageTest() {
        checkNumberedConfig(generateNumberedConfig());
    }

    @Test
    void copyTest() {
        Configuration configuration = generateNumberedConfig();

        Configuration copy = configuration.copy();

        checkNumberedConfig(copy);
    }

    @Test
    void equalsTest1() {
        Configuration configuration = generateNumberedConfig();

        assertNotEquals(configuration, new Configuration());
    }

    @Test
    void equalsTest2() {
        Configuration configuration = generateNumberedConfig();

        assertEquals(configuration, configuration.copy());
    }

    @ParameterizedTest
    @ValueSource(strings = {"foo", "banana"})
    void stringTest(String string) throws IOException {
        Configuration configuration = new Configuration();
        ConfigNode fooNode = configuration.node("foo");
        fooNode.set(string);

        loader.save(configuration);
        Configuration loaded = loader.load();

        assertEquals(string, loaded.node("foo").getAsString(), "Set value should be returned.");
    }

    @Test
    void nullTest() throws IOException {
        Configuration configuration = new Configuration();
        ConfigNode fooNode = configuration.node("foo");
        fooNode.set(null);

        loader.save(configuration);
        Configuration loaded = loader.load();

        assertNull(loaded.node("foo").get(), "Set value should be returned.");
        assertNull(loaded.node("boo").get());
    }

    @Test
    void loaderTest() throws IOException {
        Configuration configuration = generateNumberedConfig();

        loader.save(configuration);
        Configuration loaded = loader.load();

        checkNumberedConfig(loaded);
        assertTrue(configuration.equalsNode(loaded));
    }
}
