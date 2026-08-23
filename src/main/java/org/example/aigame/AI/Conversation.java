package org.example.aigame.AI;

import org.example.aigame.AI.personality.Personalities;
import org.example.aigame.AI.personality.Personality;
import org.example.aigame.AI.prompts.Prompts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Conversation {

    List<MessagePair> messages;

    private Conversation(Personality personality) {
        String systemPrompt = Prompts.PromptLookup("CHARACTER_TEMPLATE", Map.of(
                "CHARACTER_NAME", personality.getName(),
                "PERSONALITY", personality.getDescription()
        ));

        messages = new ArrayList<>();
        messages.add(new MessagePair("system", systemPrompt));
        messages.add(new MessagePair(
                personality.getName() + "'s History of the Player",
                personality.getHistory()
        ));
    }

    public static Conversation StartNewConversation(Personality personality) {
        return new Conversation(personality);
    }

    public static Conversation StartNewConversation() {
        return new Conversation(Personalities.getByName("Robert"));
    }

    public void addMessage(String role, String content) {
        messages = new ArrayList<>(messages);
        messages.add(new MessagePair(role, content));
    }

    public List<MessagePair> getMessages() {
        return messages;
    }
}