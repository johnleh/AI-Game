package org.example.aigame.AI;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.aigame.AI.prompts.Prompts;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class OllamaClient {

    public static String sendChat(Conversation conversation, String model) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "stream", false,
                "messages", conversation.getMessages()
        );

        String jsonBody = mapper.writeValueAsString(requestBody);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/chat"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode responseJson = mapper.readTree(response.body());
        return responseJson.get("message").get("content").asText();
    }

    public static String summarizeChat(Conversation conversation, String model) throws Exception {
        List<MessagePair> messages = conversation.getMessages();

        StringBuilder transcript = new StringBuilder();
        for (MessagePair msg : messages) {
            transcript.append(msg.role()).append(": ").append(msg.content()).append("\n");
        }

        String prompt = "Below is a transcript of a conversation you (the character) just had with a player " +
                "in a video game. Write a short first-person summary (2-4 sentences) of how you would " +
                "remember this conversation afterward — your impression of the player, what was said or done, " +
                "and how you feel about it. Write it as your own private thoughts, not as dialogue to the player.\n\n" +
                "Transcript:\n" + transcript;

        return sendGenerate(prompt, model);
    }

    public static String sendGenerate(String prompt, String model) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "prompt", prompt,
                "stream", false
        );

        String jsonBody = mapper.writeValueAsString(requestBody);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode responseJson = mapper.readTree(response.body());
        return responseJson.get("response").asText();
    }
}