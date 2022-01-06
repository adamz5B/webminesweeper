package pl.adamzylinski.yam.websocket;

import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import pl.adamzylinski.yam.models.BoardStateInfo;
import pl.adamzylinski.yam.models.GameMessageI;

/** Encoder of outbound messages. */
public class GameMessageEncoder implements Encoder.Text<GameMessageI> {

    @Override
    public String encode(GameMessageI gameMessage) throws EncodeException {
        if (gameMessage instanceof BoardStateInfo) {
            return ((BoardStateInfo) gameMessage).getJsonObject().toString();
        }
        return "Server error";
    }

}
