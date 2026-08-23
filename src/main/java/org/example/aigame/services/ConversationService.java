package org.example.aigame.services;

import javafx.application.Platform;
import org.example.aigame.AI.Conversation;
import org.example.aigame.AI.OllamaClient;
import org.example.aigame.AI.personality.Personality;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ConversationService {

    private static final String MODEL = "llama3.2:3b";

    private static Conversation currentConversation;
    private static Personality currentPersonality;
    private static boolean active = false;
    private static boolean waitingForResponse = false;

    public static void start(Personality personality) {
        currentPersonality = personality;
        currentConversation = Conversation.StartNewConversation(personality);
        active = true;
        waitingForResponse = false;
    }

    public static boolean isActive() {
        return active;
    }

    public static boolean isWaitingForResponse() {
        return waitingForResponse;
    }

    public static void sendMessage(String userText, Consumer<String> onReply, Consumer<Throwable> onError) {
        if (!active || waitingForResponse || userText == null || userText.isBlank()) {
            return;
        }

        currentConversation.addMessage("user", userText);
        waitingForResponse = true;

        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return OllamaClient.sendChat(currentConversation, MODEL);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .whenComplete((reply, error) -> Platform.runLater(() -> {
                    waitingForResponse = false;

                    if (error != null) {
                        onError.accept(error);
                        return;
                    }

                    currentConversation.addMessage("assistant", reply);
                    onReply.accept(reply);
                }));
    }

    public static void end() {
        if (!active) return;

        Conversation conversationToSummarize = currentConversation;
        Personality personalityToUpdate = currentPersonality;

        active = false;
        waitingForResponse = false;
        currentConversation = null;
        currentPersonality = null;

        if (conversationToSummarize == null || personalityToUpdate == null) return;

        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return OllamaClient.summarizeChat(conversationToSummarize, MODEL);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .whenComplete((summary, error) -> {
                    if (error != null) {
                        System.err.println("Failed to summarize conversation: " + error.getMessage());
                        return;
                    }
                    personalityToUpdate.setHistory(summary);
                });
    }

    public static Personality getCurrentPersonality() {
        return currentPersonality;
    }
}