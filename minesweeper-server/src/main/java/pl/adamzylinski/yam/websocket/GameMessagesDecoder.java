package pl.adamzylinski.yam.websocket;

import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;

import java.io.StringReader;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import pl.adamzylinski.yam.models.ClickType;
import pl.adamzylinski.yam.models.GameMessageI;
import pl.adamzylinski.yam.models.MoveInfo;

/** Decoder of inbound messages */
public class GameMessagesDecoder implements Decoder.Text<GameMessageI> {
    private static final String FIELD = "field";
    private static final String CURSOR = "cursor";

    private static final String X = "x";
    private static final String Y = "y";

    @Override
    public GameMessageI decode(String incomingString) throws DecodeException {
        JsonObject jsonObject = Json// NOSONAR - StringReader
                .createReader(new StringReader(incomingString)).readObject();

        return new MoveInfo(getIntOf(jsonObject, FIELD, X), getIntOf(jsonObject, FIELD, Y),
                getIntOf(jsonObject, CURSOR, X), getIntOf(jsonObject, CURSOR, Y), geClickType(jsonObject),
                getLastFrame(jsonObject));
    }

    private ClickType geClickType(JsonObject jo) {
        switch (jo.getInt("clickType")) {
            case 1:
                return ClickType.MIDDLE;
            case 2:
                return ClickType.RIGHT;
            case 0:
                return ClickType.LEFT;
            default:
                return ClickType.NONE;
        }
    }

    private long getLastFrame(JsonObject jo) {
        return jo.getJsonNumber("lastFrame").longValue();
    }

    private Integer getIntOf(JsonObject jo, String mainKey, String coordKey) {
        return jo.get(mainKey).asJsonObject().getInt(coordKey);
    }

    @Override
    public boolean willDecode(String incomingString) {
        try {
            // Check if incoming message is valid JSON
            Json.createReader(new StringReader(incomingString)).readObject();// NOSONAR - StringReader
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
