package org.example.aigame.AI.prompts;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Prompts {

    private static final String PROMPTS_DIR = "assets/prompts";
    private static final List<String> PROMPT_FILES = List.of("CHARACTER_TEMPLATE");
    private static final Map<String, String> promptMap = new HashMap<>();
    private static boolean loaded = false;

    private Prompts() {}

    public static void loadPrompts() throws IOException, URISyntaxException {
        if (loaded) return;

        URL dirUrl = Prompts.class.getClassLoader().getResource(PROMPTS_DIR);
        if (dirUrl == null) {
            throw new IOException("Prompts directory not found on classpath: " + PROMPTS_DIR);
        }

        Path dirPath = Paths.get(dirUrl.toURI());

        for (String filename : PROMPT_FILES) {
            Path filePath = dirPath.resolve(filename);
            if (!Files.exists(filePath)) {
                throw new IOException("Missing prompt file: " + filePath);
            }
            String content = Files.readString(filePath, StandardCharsets.UTF_8);
            promptMap.put(filename, content);
        }

        loaded = true;
    }

    public static String PromptLookup(String key) {
        String prompt = promptMap.get(key);
        if (prompt == null) {
            throw new IllegalArgumentException("No prompt found for key: " + key);
        }
        return prompt;
    }

    public static String PromptLookup(String key, Map<String, String> replacers) {
        String prompt = promptMap.get(key);
        if (prompt == null) {
            throw new IllegalArgumentException("No prompt found for key: " + key);
        }

        if (replacers == null || replacers.isEmpty()) {
            return prompt;
        }

        String result = prompt;
        for (Map.Entry<String, String> entry : replacers.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
}