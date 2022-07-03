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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationLoaderTest {

    private static GsonConfigurationLoader loader;
    private static File testFile;

    @BeforeAll
    private static void init() {
        testFile = generateTestFile();
        while (testFile.exists())
            testFile = generateTestFile();
        loader = GsonConfigurationLoader
            .builder()
            .adapter(new ConfigAdapter(new GsonBuilder()
                .serializeNulls()
                .create()))
            .path(testFile.toPath())
            .build();
    }

    private static File generateTestFile() {
        return new File("Test-"+ UUID.randomUUID().toString().substring(0, 8)+".json");
    }

    @AfterAll
    private static void clear() throws IOException {
        System.gc();
        try {
            Files.delete(testFile.toPath());
        } catch (Exception e) {
            System.err.println("File for testing could not be deleted. Please do it manually. (" + testFile.getAbsolutePath() + ")");
            throw e;
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"foo", "banana"})
    void stringTest(String str) throws IOException {
        Configuration configuration = new Configuration()
            .set("foo", str);

        loader.save(configuration);
        Configuration loaded = loader.load();

        assertEquals(str, loaded.get("foo", String.class), "Set value should be returned.");
    }

    @Test
    void nullTest() throws IOException {
        Configuration configuration = new Configuration()
            .set("foo", null);

        loader.save(configuration);
        Configuration loaded = loader.load();

        assertNull(loaded.get("foo", Object.class), "Set value should be returned.");
        assertNull(loaded.get("boo"));
    }

    @Test
    void subClusterTest() throws IOException {
        Configuration configuration = new Configuration()
            .set("number.zero", 0)
            .set("number.one", 1)
            .set("text.zero", "zero")
            .set("text.one", "one");

        loader.save(configuration);
        Configuration loaded = loader.load();

        assertEquals(0, loaded.get("number.zero", Double.class).intValue());
        assertEquals(1, loaded.get("number.one", Double.class).intValue());
        assertEquals("zero", loaded.get("text.zero", String.class));
        assertEquals("one", loaded.get("text.one", String.class));
    }

    @Test
    void selectorTest() throws IOException {
        Configuration configuration = new Configuration()
            .set("english.house", "House")
            .set("danish.house", "Hus")
            .set("*.modernLanguage", true)
            .set("latin.house", "domum")
            .set("latin.modernLanguage", false);


        loader.save(configuration);
        Configuration loaded = loader.load();

        assertTrue(loaded.get("english.modernLanguage", Boolean.class));
        assertFalse(loaded.get("latin.modernLanguage", Boolean.class));
        assertEquals("Hus", loaded.get("danish.house"));
    }
}
