package com.maff.codingcounter.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Used to use JSON to store statistics before migrating to IntelliJ PersistentStateComponent.
 * Now is used only to preload legacy date after plugin update.
 */
public class LegacyStatsRepository {
    private String fileName;
    private Gson gson;

    public LegacyStatsRepository(String fileName) {
        this.fileName = fileName;

        gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    public CodingStats load() throws IOException {
        Path file = Paths.get(fileName);

        if(!Files.exists(file)) {
            return new CodingStats();
        }

        String json = new String(Files.readAllBytes(file));

        try {
            return gson.fromJson(json, CodingStats.class);
        }
        catch (JsonSyntaxException e) {
            throw new IOException("Can't deserialize JSON", e);
        }
    }
}
