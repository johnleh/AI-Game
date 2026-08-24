package org.example.aigame.AI.personality;

public class Personality {
    private final String name;
    private final String description;
    private final String greeting;
    private final String portrait;

    private String history;

    protected Personality(String name, String description) {
        this(name, description, null, null);
    }

    protected Personality(String name, String description, String portrait) {
        this(name, description, portrait, null);
    }

    protected Personality(String name, String description, String portrait, String greeting) {
        this.name = name;
        this.description = description;
        this.greeting = (greeting != null) ? greeting : name + " looks at you";
        this.portrait = portrait;
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

    public String getPortrait() {
        return portrait;
    }

    /**
     * Classpath-resolvable path to this personality's portrait image,
     * or null if no portrait was specified.
     */
    public String getPortraitResourcePath() {
        if (portrait == null || portrait.isBlank()) {
            return null;
        }
        return "assets/portraits/" + portrait;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }
}