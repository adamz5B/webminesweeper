package pl.adamzylinski.yam.models;

import java.util.HashMap;
import java.util.Map;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import pl.adamzylinski.yam.game.BoardField;

/**
 * Representation of board state which is send to player.
 */
public class BoardStateInfo implements GameMessageI, JsonObjectifyI {
    private PlayerInfo player;
    private PlayerInfo opponentPlayer;
    private PointerInfo opponentPointerInfo;
    private Map<XYcoord<Integer>, BoardField> changedFields = new HashMap<>();
    private boolean over = false;
    private long timeFrame = 0;

    public BoardStateInfo(PlayerInfo player, PlayerInfo opponent, PointerInfo opponentPointerInfo,
            Map<XYcoord<Integer>, BoardField> changedFields, long timeFrame) {
        this.player = player;
        this.opponentPlayer = opponent;
        this.opponentPointerInfo = opponentPointerInfo;
        this.changedFields = changedFields;
        this.timeFrame = timeFrame;
    }

    public BoardStateInfo setGameOver() {
        over = true;
        return this;
    }

    @Override
    public JsonObject getJsonObject() {
        JsonObjectBuilder job = Json.createObjectBuilder().add("player", player.getJsonObject())
                .add("opponent", opponentPlayer.getJsonObject())
                .add("pointer", opponentPointerInfo.getJsonObject()).add("changedFields", getJsonMap())
                .add("timeFrame", timeFrame);
        if (over) {
            job.add("over", over);
        }
        return job.build();
    }

    private JsonArray getJsonMap() {
        JsonArrayBuilder jab = Json.createArrayBuilder();
        changedFields.keySet().stream()
                .forEach(key -> jab.add(Json.createObjectBuilder()
                        .add("key", Json.createObjectBuilder().add("x", key.getX()).add("y", key.getY()))
                        .add("value", changedFields.get(key).getJsonObject())));
        return jab.build();
    }

}
