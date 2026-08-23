package org.example.aigame.main;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.example.aigame.AI.personality.Personalities;
import org.example.aigame.AI.prompts.Prompts;
import org.example.aigame.Constants;
import org.example.aigame.component.DialogComponent;
import org.example.aigame.entities.EntityTypeEnum;
import org.example.aigame.entities.GameEntityFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.Map;

import static com.almasb.fxgl.dsl.FXGL.*;

public class AIGameApplication extends GameApplication {

    private static final int WALL_THICKNESS = 32;

    @Override
    protected void initSettings(GameSettings settings) {
        setConstantSetting(settings);
    }

    private void setConstantSetting(GameSettings settings) {
        settings.setWidth(640);
        settings.setHeight(480);
        settings.setTitle("AI Game");
        settings.setVersion("0.1");
    }

    @Override
    protected void initInput() {
        onKey(KeyCode.D, () -> {
            player.translateX(Constants.PLAYER_SPEED);
            clampPlayerToBounds();
        });

        onKey(KeyCode.A, () -> {
            player.translateX(-Constants.PLAYER_SPEED);
            clampPlayerToBounds();
        });

        onKey(KeyCode.W, () -> {
            player.translateY(-Constants.PLAYER_SPEED);
            clampPlayerToBounds();
        });

        onKey(KeyCode.S, () -> {
            player.translateY(Constants.PLAYER_SPEED);
            clampPlayerToBounds();
        });

        bindInteractionInput();
    }

    private void bindInteractionInput() {
        onKeyDown(KeyCode.E, this::handleInteract);
    }

    private void handleInteract() {
        Entity target = findClosestTalkableEntityInRange();
        if (target != null) {
            DialogComponent dialog = target.getComponent(DialogComponent.class);
            dialog.openDialog();
        }
    }

    private Entity findClosestTalkableEntityInRange() {
        return getGameWorld().getEntitiesByType(EntityTypeEnum.NPC).stream()
                .filter(e -> e.hasComponent(DialogComponent.class))
                .filter(e -> e.getComponent(DialogComponent.class).isInRange())
                .min(Comparator.comparingDouble(e -> e.getCenter().distance(player.getCenter())))
                .orElse(null);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {}

    private Entity player;
    private Entity npc;

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new GameEntityFactory());
        try {
            Prompts.loadPrompts();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        createBasicFloor();
        createBasicWall();

        player = spawn("player", new SpawnData(300, 300).put("sprite", "PERSON1.png"));
        npc = spawn("npc", new SpawnData(400, 300)
                .put("sprite", "PERSON2.png")
                .put("personality", Personalities.getByName("Robert")));
    }

    @Override
    protected void initUI() {
    }

    private void createBasicFloor() {
        int tileSize = 64;
        int cols = getAppWidth() / tileSize;
        int rows = (getAppHeight() / tileSize) + 1;

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                entityBuilder()
                        .at(x * tileSize, y * tileSize)
                        .view(texture("WOOD_FLOOR.png"))
                        .buildAndAttach();
            }
        }
    }

    private void createBasicWall() {
        int width = getAppWidth();
        int height = getAppHeight();

        entityBuilder()
                .at(0, 0)
                .view(new Rectangle(width, WALL_THICKNESS, Color.DIMGRAY))
                .buildAndAttach();

        entityBuilder()
                .at(0, height - WALL_THICKNESS)
                .view(new Rectangle(width, WALL_THICKNESS, Color.DIMGRAY))
                .buildAndAttach();

        entityBuilder()
                .at(0, 0)
                .view(new Rectangle(WALL_THICKNESS, height, Color.DIMGRAY))
                .buildAndAttach();

        entityBuilder()
                .at(width - WALL_THICKNESS, 0)
                .view(new Rectangle(WALL_THICKNESS, height, Color.DIMGRAY))
                .buildAndAttach();
    }

    private void clampPlayerToBounds() {
        double minX = WALL_THICKNESS;
        double minY = WALL_THICKNESS;
        double maxX = getAppWidth() - WALL_THICKNESS - player.getWidth();
        double maxY = getAppHeight() - WALL_THICKNESS - player.getHeight();

        player.setX(Math.max(minX, Math.min(player.getX(), maxX)));
        player.setY(Math.max(minY, Math.min(player.getY(), maxY)));
    }

    public static void main(String[] args) {
        launch(args);
    }
}