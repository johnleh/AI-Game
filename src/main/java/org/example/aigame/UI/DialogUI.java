package org.example.aigame.UI;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.function.Consumer;

import static com.almasb.fxgl.dsl.FXGL.*;

public class DialogUI {

    private static final double BOX_WIDTH = 420;
    private static final double BOX_HEIGHT = 280;
    private static final double HISTORY_HEIGHT = 160;
    private static final String FONT_FAMILY = "Arial";
    private static final double FONT_SIZE = 14;

    private final VBox root;
    private final VBox historyContainer;
    private final ScrollPane historyScrollPane;
    private final TextField input;
    private final Button submitButton;
    private final Label statusLabel;

    private Consumer<String> onSubmit;
    private Runnable onExit;

    public DialogUI() {
        historyContainer = new VBox(6);
        historyContainer.setPadding(new Insets(8));

        historyScrollPane = new ScrollPane(historyContainer);
        historyScrollPane.setFitToWidth(true);
        historyScrollPane.setPrefViewportHeight(HISTORY_HEIGHT);
        historyScrollPane.setStyle(
                "-fx-background: #333333; -fx-background-color: #333333; " +
                        "-fx-control-inner-background: #333333; -fx-border-color: black; -fx-border-width: 2;"
        );

        statusLabel = new Label();
        statusLabel.setTextFill(Color.LIGHTGRAY);
        statusLabel.setFont(Font.font(FONT_FAMILY, 12));
        statusLabel.setVisible(false);

        input = new TextField();
        input.setPromptText("Say something...");
        input.setPrefWidth(280);
        input.setOnAction(e -> handleSubmit());

        submitButton = new Button("⬆");
        submitButton.setOnAction(e -> handleSubmit());

        Button exitButton = new Button("X");
        exitButton.setStyle(
                "-fx-background-color: #cc0000; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold;"
        );
        exitButton.setOnAction(e -> {
            if (onExit != null) {
                onExit.run();
            }
        });

        HBox inputRow = new HBox(10, input, submitButton, exitButton);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        HBox buttonRow = new HBox(10, statusLabel);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);

        VBox content = new VBox(10, historyScrollPane, inputRow, buttonRow);
        content.setPadding(new Insets(15));
        content.setStyle(
                "-fx-background-color: rgba(20,20,20,0.95); " +
                        "-fx-border-color: black; " +
                        "-fx-border-width: 2; " +
                        "-fx-background-radius: 10; " +
                        "-fx-border-radius: 10;"
        );
        content.setPrefWidth(BOX_WIDTH);

        root = new VBox(content);
        root.setVisible(false);

        double x = (getAppWidth() - BOX_WIDTH) / 2.0;
        double y = (getAppHeight() - BOX_HEIGHT) / 2.0;
        root.setTranslateX(x);
        root.setTranslateY(y);
    }

    public void attach() {
        getGameScene().addUINode(root);
    }

    public void detach() {
        getGameScene().removeUINode(root);
    }

    public void setOnSubmit(Consumer<String> onSubmit) {
        this.onSubmit = onSubmit;
    }

    public void setOnExit(Runnable onExit) {
        this.onExit = onExit;
    }

    public void show() {
        root.setVisible(true);
        Platform.runLater(input::requestFocus);
    }

    public void hide() {
        root.setVisible(false);
    }

    public void clearHistory() {
        historyContainer.getChildren().clear();
    }

    public void addLine(String speaker, String text, boolean isPlayer) {
        TextFlow flow = new TextFlow();

        Text speakerText = new Text(speaker + ": ");
        speakerText.setFill(isPlayer ? Color.LIGHTBLUE : Color.WHITE);
        speakerText.setStyle(
                "-fx-font-family: '" + FONT_FAMILY + "'; " +
                        "-fx-font-size: " + FONT_SIZE + "px; " +
                        "-fx-font-weight: bold;"
        );

        Text bodyText = new Text(text);
        bodyText.setFill(Color.WHITE);
        bodyText.setStyle(
                "-fx-font-family: '" + FONT_FAMILY + "'; " +
                        "-fx-font-size: " + FONT_SIZE + "px;"
        );

        flow.getChildren().addAll(speakerText, bodyText);
        flow.setPrefWidth(BOX_WIDTH - 60);

        historyContainer.getChildren().add(flow);

        // Wait until JavaFX has laid out the newly added response,
        // then move the scroll position all the way to the bottom.
        Platform.runLater(() -> {
            historyContainer.applyCss();
            historyContainer.layout();
            historyScrollPane.layout();
            historyScrollPane.setVvalue(1.0);
        });
    }

    public void setWaiting(boolean waiting) {
        submitButton.setDisable(waiting);
        input.setDisable(waiting);
        statusLabel.setText(waiting ? "Thinking..." : "");
        statusLabel.setVisible(waiting);
    }

    public void clearInput() {
        input.clear();
    }

    private void handleSubmit() {
        String text = input.getText();

        if (text == null || text.isBlank()) {
            return;
        }

        if (onSubmit != null) {
            onSubmit.accept(text);
        }
    }
}