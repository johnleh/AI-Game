package org.example.aigame.component;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.example.aigame.entities.EntityTypeEnum;
import org.example.aigame.AI.personality.Personality;

import static com.almasb.fxgl.dsl.FXGL.*;

public class DialogComponent extends Component {

    private static final int TRIGGER_DISTANCE = 64;
    private static final int BUBBLE_Y_OFFSET = 20;
    private static final int BUBBLE_WIDTH = 30;
    private static final int BUBBLE_Z_INDEX = Integer.MAX_VALUE;

    private static final int DIALOG_WIDTH = 400;
    private static final int DIALOG_HEIGHT = 140;

    private final Personality personality;

    private Entity bubble;
    private boolean bubbleVisible = false;

    private VBox dialogBox;
    private boolean dialogOpen = false;

    public DialogComponent(Personality personality) {
        this.personality = personality;
    }

    public Personality getPersonality() {
        return personality;
    }

    @Override
    public void onUpdate(double tpf) {
        getGameWorld().getEntitiesByType(EntityTypeEnum.PLAYER).stream()
                .findFirst()
                .ifPresent(this::checkProximity);
    }

    private void checkProximity(Entity player) {
        Point2D npcCenter = entity.getCenter();
        Point2D playerCenter = player.getCenter();

        double distance = npcCenter.distance(playerCenter);

        if (distance <= TRIGGER_DISTANCE) {
            showBubble();
        } else {
            hideBubble();
        }
    }

    public boolean isInRange() {
        return bubbleVisible;
    }

    public boolean isDialogOpen() {
        return dialogOpen;
    }

    public void openDialog() {
        if (dialogOpen) return;
        dialogOpen = true;

        Label greetingLabel = new Label(personality.getGreeting());
        greetingLabel.setFont(Font.font(16));
        greetingLabel.setTextFill(Color.BLACK);
        greetingLabel.setWrapText(true);
        greetingLabel.setMaxWidth(360);

        TextField input = new TextField();
        input.setPrefWidth(280);

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> handleSubmit(input.getText()));

        Button exitButton = new Button("X");
        exitButton.setStyle(
                "-fx-background-color: #cc0000;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;"
        );
        exitButton.setOnAction(e -> closeDialog());

        HBox inputRow = new HBox(10, input, exitButton);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        HBox buttonRow = new HBox(10, submitButton);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);

        Rectangle background = new Rectangle(DIALOG_WIDTH, DIALOG_HEIGHT, Color.rgb(255, 255, 255, 0.95));
        background.setStroke(Color.BLACK);
        background.setArcWidth(12);
        background.setArcHeight(12);

        VBox content = new VBox(12, greetingLabel, inputRow, buttonRow);
        content.setPadding(new Insets(15));
        content.setMaxWidth(DIALOG_WIDTH);

        dialogBox = new VBox(new Group(background, content));

        double x = (getAppWidth() - DIALOG_WIDTH) / 2.0;
        double y = (getAppHeight() - DIALOG_HEIGHT) / 2.0;
        dialogBox.setTranslateX(x);
        dialogBox.setTranslateY(y);

        getGameScene().addUINode(dialogBox);

        getInput().setProcessInput(false);
        javafx.application.Platform.runLater(input::requestFocus);
    }

    private void handleSubmit(String text) {
        System.out.println(personality.getName() + " received: " + text);
        // TODO: hook this into your AI/dialog backend
        closeDialog();
    }

    private void closeDialog() {
        if (!dialogOpen) return;

        if (dialogBox != null) {
            getGameScene().removeUINode(dialogBox);
            dialogBox = null;
        }
        dialogOpen = false;

        getInput().setProcessInput(true);
    }

    private void showBubble() {
        if (bubbleVisible) {
            repositionBubble();
            return;
        }

        bubble = new Entity();
        bubble.getViewComponent().addChild(createBubbleGraphic());
        bubble.setZIndex(BUBBLE_Z_INDEX);
        getGameWorld().addEntity(bubble);

        repositionBubble();
        bubbleVisible = true;
    }

    private void hideBubble() {
        if (!bubbleVisible) return;

        if (bubble != null) {
            getGameWorld().removeEntity(bubble);
            bubble = null;
        }
        bubbleVisible = false;
    }

    private void repositionBubble() {
        if (bubble == null) return;

        double npcCenterX = entity.getCenter().getX();

        double x = npcCenterX - BUBBLE_WIDTH / 2;
        double y = entity.getY() - BUBBLE_Y_OFFSET;

        bubble.setPosition(x, y);
    }

    private Group createBubbleGraphic() {
        Rectangle background = new Rectangle(BUBBLE_WIDTH, 20, Color.WHITE);
        background.setStroke(Color.BLACK);
        background.setArcWidth(10);
        background.setArcHeight(10);

        Text text = new Text("E");
        text.setFont(Font.font(14));
        text.setX(12);
        text.setY(15);

        return new Group(background, text);
    }

    @Override
    public void onRemoved() {
        hideBubble();
        closeDialog();
    }
}