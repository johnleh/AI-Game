package org.example.aigame.AI.personality;

public class Personality {
    private final String name;
    private final String description;
    private final String greeting;

    private String history;

    protected Personality(String name, String description) {
        this(name, description, null);
    }

    protected Personality(String name, String description, String greeting) {
        this.name = name;
        this.description = description;
        this.greeting = (greeting != null) ? greeting : name + " looks at you";
        this.history = "";
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getGreeting() {
        return greeting;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }
}