package org.example.aigame.component;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.example.aigame.services.ConversationService;
import org.example.aigame.AI.personality.Personality;
import org.example.aigame.UI.DialogUI;
import org.example.aigame.entities.EntityTypeEnum;

import static com.almasb.fxgl.dsl.FXGL.*;

public class DialogComponent extends Component {

    private static final int TRIGGER_DISTANCE = 64;
    private static final int BUBBLE_Y_OFFSET = 20;
    private static final int BUBBLE_WIDTH = 30;
    private static final int BUBBLE_Z_INDEX = Integer.MAX_VALUE;

    private final Personality personality;

    private Entity bubble;
    private boolean bubbleVisible = false;

    private DialogUI dialogUI;
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

        ensureDialogUI();

        ConversationService.start(personality);

        dialogUI.clearHistory();
        dialogUI.addLine(personality.getName(), personality.getGreeting(), false);
        dialogUI.setWaiting(false);
        dialogUI.attach();
        dialogUI.show();

        getInput().setProcessInput(false);
    }

    private void ensureDialogUI() {
        if (dialogUI != null) return;

        dialogUI = new DialogUI();
        dialogUI.setOnSubmit(this::handleSubmit);
        dialogUI.setOnExit(this::closeDialog);
    }

    private void handleSubmit(String text) {
        dialogUI.addLine("You", text, true);
        dialogUI.clearInput();
        dialogUI.setWaiting(true);

        ConversationService.sendMessage(text,
                reply -> {
                    dialogUI.setWaiting(false);
                    dialogUI.addLine(personality.getName(), reply, false);
                },
                error -> {
                    dialogUI.setWaiting(false);
                    dialogUI.addLine(personality.getName(), "...(no response)", false);
                    System.err.println("Ollama error: " + error.getMessage());
                }
        );
    }

    private void closeDialog() {
        if (!dialogOpen) return;

        ConversationService.end();

        dialogUI.hide();
        dialogUI.detach();
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