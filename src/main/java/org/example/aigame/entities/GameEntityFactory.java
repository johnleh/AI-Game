package org.example.aigame.entities;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import org.example.aigame.AI.personality.Personality;
import org.example.aigame.component.DepthComponent;
import org.example.aigame.component.DialogComponent;
import org.example.aigame.entities.EntityTypeEnum;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;

public class GameEntityFactory implements EntityFactory {

    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        String sprite = data.get("sprite");

        return entityBuilder(data)
                .type(EntityTypeEnum.PLAYER)
                .viewWithBBox(sprite)
                .with(new DepthComponent())
                .build();
    }

    @Spawns("npc")
    public Entity newNpc(SpawnData data) {
        String sprite = data.get("sprite");
        Personality personality = data.get("personality");

        return entityBuilder(data)
                .type(EntityTypeEnum.NPC)
                .viewWithBBox(sprite)
                .with(new DialogComponent(personality))
                .with(new DepthComponent())
                .build();
    }
}