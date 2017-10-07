package com.maff.codingcounter.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JsonStatsRepository implements StatsRepository {
    private String fileName;
    private Gson gson;

    public JsonStatsRepository(String fileName) {
        this.fileName = fileName;

        gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    @Override
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

    @Override
    public void save(CodingStats stats) throws IOException {
        Path file = Paths.get(fileName);

        if(!Files.exists(file)) {
            Files.createDirectories(file.getParent());

            try {
                Files.createFile(file);
            } catch (FileAlreadyExistsException e) {
                //ignore
            }
        }

        String json = gson.toJson(stats);
        Files.write(file, json.getBytes());
    }
}
