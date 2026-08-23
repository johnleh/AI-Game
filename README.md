# AI-Game

A proof-of-concept 2D Java game exploring local LLM integration in real-time gameplay. Built with [FXGL](https://github.com/AlmasB/FXGL) for the game engine and [Ollama](https://ollama.com/) for on-device AI inference. The goal is NPCs that hold live, dynamically generated conversations — powered entirely by a locally-hosted model, with no cloud calls involved.

> **Status: early development.** Core movement, NPC interaction, and AI-driven dialog are in place and working. Expect rough edges, missing features, and frequent changes.

## What's working right now

- Top-down player movement (WASD) with wall collision
- Proximity-based NPC interaction — walk near an NPC and a prompt appears above them
- Press **E** to open a dialog window with the nearest interactable NPC
- Custom in-game dialog UI (text input, submit button, exit button) built on FXGL's scene graph
- **Live AI-generated NPC responses** — player input is sent to a locally-running Ollama model and the generated response is displayed in the dialog UI in real time
- Per-NPC `Personality` system (name, description, greeting) feeding context into prompts so each NPC's tone/knowledge stays consistent
- Z-index depth sorting so entities render correctly relative to their Y position

## What's in progress

- Conversation history per NPC (the groundwork exists in `Personality`, not yet wired into generation)
- Take in Ollama endpoint and model in a cleaner fashion (currently hardcoded in `OllamaClient`)
- Error handling for when Ollama is offline or unreachable

## Tech stack

| Component | Version |
|---|---|
| Java | 25 |
| FXGL | 11.17 |
| JavaFX (controls, fxml, media) | 21.0.6 |
| Jackson Databind | 2.17.0 |
| Build tool | Maven |

## Requirements

- JDK 25+
- Maven (or use the included `mvnw` / `mvnw.cmd` wrapper)
- [Ollama](https://ollama.com/) installed and running locally, with a model pulled (e.g. `ollama pull llama3.2`)

## Running the game

```bash
./mvnw clean javafx:run
```

(or `mvnw.cmd clean javafx:run` on Windows)

This runs the `AIGameApplication` main class via the JavaFX Maven plugin.

## Controls

| Key | Action |
|---|---|
| W / A / S / D | Move |
| E | Interact with nearby NPC |

## Disclaimer

This is a personal proof-of-concept project for exploring local LLM + game engine integration. It is not a finished game and APIs/structure will change without notice.