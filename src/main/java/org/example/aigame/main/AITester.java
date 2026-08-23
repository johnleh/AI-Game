package org.example.aigame.main;

import org.example.aigame.AI.Conversation;
import org.example.aigame.AI.OllamaClient;
import org.example.aigame.AI.prompts.Prompts;

import java.util.Scanner;

public class AITester {
    public static void main(String[] args) throws Exception {
        Prompts.loadPrompts();
        OllamaClient client = new OllamaClient();
        Conversation conversation = Conversation.StartNewConversation();
        Scanner scanner = new Scanner(System.in);
        String userInput = "";
        while (!"exit".equalsIgnoreCase(userInput) && !"quit".equalsIgnoreCase(userInput)) {
            System.out.print("You: ");
            userInput = scanner.nextLine();
            if ("exit".equalsIgnoreCase(userInput) || "quit".equalsIgnoreCase(userInput)) break;

            conversation.addMessage("user", userInput);
            String reply = client.sendChat(conversation, "llama3.2:3b");
            conversation.addMessage("assistant", reply);
            System.out.println("character: " + reply);
        }
        System.out.println("SESSION OVER");
        System.out.println(client.summarizeChat(conversation,"llama3.2:3b"));
    }

}
