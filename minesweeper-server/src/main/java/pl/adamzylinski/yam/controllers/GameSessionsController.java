package pl.adamzylinski.yam.controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.inject.Named;
import pl.adamzylinski.yam.game.GameState;
import pl.adamzylinski.yam.game.MinesweeperGame;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

@Named
@ApplicationScoped
public class GameSessionsController implements Serializable {

    private List<MinesweeperGame> games = null;

    @PostConstruct
    public void init() {
        games = new ArrayList<>();
    }

    public MinesweeperGame newPlayer(String playerId) {
        MinesweeperGame game;
        if (games.stream().anyMatch(g -> g.getGameState() == GameState.WAIT_FOR_PLAYER)) {
            game = games.stream().filter(g -> g.getGameState() == GameState.WAIT_FOR_PLAYER)// NOSONAR - availability
                                                                                            // checked by anyMatch
                    .findFirst().get();
            game.startGameWithPlayerTwo(playerId);

        } else {
            game = new MinesweeperGame(playerId);
            games.add(game);
        }
        return game;
    }

    public MinesweeperGame getGameForPlayer(String playerId) {
        Optional<MinesweeperGame> foundGame = games.stream()
                .filter(game -> game.getPlayers().stream().anyMatch(player -> player.getId().equals(playerId)))
                .findFirst();
        if (foundGame.isPresent()) {
            return foundGame.get();
        }
        return null;
    }

    public MinesweeperGame notifyOnDisconnect(String playerId) {
        getGameForPlayer(playerId).endTheGame();
        return getGameForPlayer(playerId);
    }

}
