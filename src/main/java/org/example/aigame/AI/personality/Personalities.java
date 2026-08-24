package org.example.aigame.AI.personality;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class Personalities {

    private static final String PERSONALITIES_PATH = "assets/personalities";

    private static List<Personality> personalities;

    public static List<Personality> getAll() {
        if (personalities == null) {
            personalities = loadPersonalitiesJSON();
        }
        return personalities;
    }

    public static Personality getByName(String name) {
        return getAll().stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    private static List<Personality> loadPersonalitiesJSON() {
        List<Personality> result = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            List<String> resources = listJsonResources(PERSONALITIES_PATH);
            for (String resourcePath : resources) {
                try (InputStream is = Personalities.class.getModule().getResourceAsStream(resourcePath)) {                    System.out.println("DEBUG: opening " + resourcePath + " -> stream is " + (is == null ? "NULL" : "OK"));
                    if (is == null) continue;

                    JsonNode node = mapper.readTree(is);

                    String name = node.has("name") ? node.get("name").asText() : null;
                    String description = node.has("description") ? node.get("description").asText() : "";
                    String portrait = node.has("portrait") ? node.get("portrait").asText() : null;
                    String greeting = node.has("greeting") ? node.get("greeting").asText() : null;

                    if (name == null || name.isBlank()) continue;

                    Personality personality = new Personality(name, description, portrait, greeting);

                    if (node.has("history")) {
                        personality.setHistory(node.get("history").asText());
                    }

                    result.add(personality);
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Failed to load personalities from " + PERSONALITIES_PATH, e);
        }

        return Collections.unmodifiableList(result);
    }

    private static List<String> listJsonResources(String folder) throws IOException, URISyntaxException {
        List<String> result = new ArrayList<>();

        URL folderUrl = Personalities.class.getClassLoader().getResource(folder);
        if (folderUrl == null) {
            return result;
        }

        URI uri = folderUrl.toURI();

        if ("jar".equals(uri.getScheme())) {
            FileSystem fs;
            boolean createdHere = false;
            try {
                fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
                createdHere = true;
            } catch (FileSystemAlreadyExistsException e) {
                fs = FileSystems.getFileSystem(uri);
            }
            try {
                Path jarFolderPath = fs.getPath(folder);
                collectJsonPaths(jarFolderPath, folder, result);
            } finally {
                if (createdHere) fs.close();
            }
        } else {
            Path dirPath = Paths.get(uri);
            try (Stream<Path> paths = Files.list(dirPath)) {
                paths.filter(p -> p.toString().endsWith(".json"))
                        .forEach(p -> result.add(folder + "/" + p.getFileName().toString()));
            }
        }

        return result;
    }

    private static void collectJsonPaths(Path jarFolderPath, String folder, List<String> result) throws IOException {
        try (Stream<Path> paths = Files.list(jarFolderPath)) {
            paths.filter(p -> p.toString().endsWith(".json"))
                    .forEach(p -> result.add(folder + "/" + p.getFileName().toString()));
        }
    }
}