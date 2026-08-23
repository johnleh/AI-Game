package org.example.aigame.AI;

import org.example.aigame.AI.personality.Personalities;
import org.example.aigame.AI.personality.Personality;
import org.example.aigame.AI.prompts.Prompts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Conversation {

    List<MessagePair> messages;


    private Conversation() {
        List<Personality> all = Personalities.getAll();
        System.out.println(all);
        Personality bob = Personalities.getByName("Robert");
        String systemPrompt = Prompts.PromptLookup("CHARACTER_TEMPLATE", Map.of(
                "CHARACTER_NAME", bob.getName(),
                "PERSONALITY", bob.getDescription()
        ));
        messages = List.of(new MessagePair("system", systemPrompt));
        System.out.println("=== SYSTEM PROMPT ===");
        System.out.println(messages);
        System.out.println("======================");
    }

    public static Conversation StartNewConversation() {
        return new Conversation();
    }

    public void addMessage(String role, String content) {
        messages = new ArrayList<>(messages);
        messages.add(new MessagePair(role, content));
    }

    public List<MessagePair> getMessages() {
        return messages;
    }


}
