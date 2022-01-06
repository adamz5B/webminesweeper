package pl.adamzylinski.yam.websocket;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

import jakarta.inject.Inject;
import jakarta.websocket.EncodeException;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import pl.adamzylinski.yam.controllers.GameSessionsController;
import pl.adamzylinski.yam.game.GameState;
import pl.adamzylinski.yam.game.MinesweeperGame;
import pl.adamzylinski.yam.models.GameMessageI;
import pl.adamzylinski.yam.models.MoveInfo;

/** End point of game server. */
@ServerEndpoint(value = "/socket", decoders = GameMessagesDecoder.class, encoders = GameMessageEncoder.class)
public class MinesweeperGameServerEndpoint {
    private static final Logger logger = Logger.getLogger(MinesweeperGameServerEndpoint.class.getName());
    @Inject
    GameSessionsController gameSessionsController;

    private static Set<Session> players = new CopyOnWriteArraySet<>();
    private static final Object lock = new Object();

    @OnOpen
    public void onJoin(Session session) {
        gameSessionsController.newPlayer(session.getId());

        players.add(session);
        logger.info("Websocket opened for " + session.getId());
    }

    @OnMessage
    public void recievedData(Session session, GameMessageI message) {
        MoveInfo moveInfo;
        if (message instanceof MoveInfo) {
            moveInfo = (MoveInfo) message;
        } else {
            return;
        }
        MinesweeperGame game = gameSessionsController.getGameForPlayer(session.getId());
        if (game != null) {
            if (game.getGameState() == GameState.IN_PROGRESS) {
                game.applyMove(session.getId(), moveInfo);
                broadcast(game.getGameStateForPlayer(session.getId()), List.of(session.getId()));
                broadcast(game.getGameStateForOpponent(session.getId()),
                        List.of(game.getOpponent(session.getId()).getId()));
            } else if (game.getGameState() == GameState.WAIT_FOR_PLAYER) {

            } else if (game.getGameState() == GameState.TERMINATED) {
                broadcast(game.getGameStateForPlayer(session.getId()).setGameOver(), List.of(session.getId()));
                broadcast(game.getGameStateForOpponent(session.getId()).setGameOver(),
                        List.of(game.getOpponent(session.getId()).getId()));
            }
        }
    }

    @OnClose
    public void close(Session session) {
        players.remove(session);
        gameSessionsController.notifyOnDisconnect(session.getId());
        logger.warning("Connection closed for " + session.getId());

    }

    private static void broadcast(GameMessageI message, List<String> recepients) {
        players.stream().filter(player -> recepients.contains(player.getId())).forEach(session -> {
            synchronized (lock) {
                try {
                    session.getBasicRemote().sendObject(message);
                } catch (IOException | EncodeException e) {
                    logger.severe("Error on broadcast: " + e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }
        });
    }

}
