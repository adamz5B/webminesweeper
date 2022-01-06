package pl.adamzylinski.yam.game;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import pl.adamzylinski.yam.models.BoardStateInfo;
import pl.adamzylinski.yam.models.MoveInfo;
import pl.adamzylinski.yam.models.PlayerInfo;
import pl.adamzylinski.yam.models.PointerInfo;
import pl.adamzylinski.yam.models.XYcoord;

/**
 * Runs minesweeper game.
 */
public class MinesweeperGame implements Serializable {
    private static final Logger logger = Logger.getLogger(MinesweeperGame.class.getName());

    private static final int BUFFER_THRESHOLD = 60;
    private static final long REVTIME = 1500;

    private static final String OWNERSHIP_CODE_PLAYER = "y";
    private static final String OWNERSHIP_CODE_OPPONENT = "o";

    /** State of the game */
    private GameState state;
    /** First player */
    private transient Player playerOne;
    /** Second player */
    private transient Player playerTwo;
    /** Game board */
    private transient GameBoard board;
    /** Buffer of affected fields */
    private transient Map<XYcoord<Integer>, BoardFieldInfo> changedFields;
    /** Timestamp of the game start */
    private long start;
    private long stop;

    /**
     * Creates game.
     * 
     * @param playerOneId ID of the first player
     */
    public MinesweeperGame(String playerOneId) {
        this.playerOne = new Player(playerOneId);
        this.state = GameState.WAIT_FOR_PLAYER;
        this.changedFields = new HashMap<>();
    }

    /**
     * Starts game when second player is provided.
     * 
     * @param playerTwoId ID of the second player.
     */
    public void startGameWithPlayerTwo(String playerTwoId) {
        this.playerTwo = new Player(playerTwoId);
        startTheGame();
    }

    /**
     * Provides state of the game
     * 
     * @return {@link GameState} of a game.
     */
    public GameState getGameState() {
        return state;
    }

    /**
     * Gets both players in form of a list
     * 
     * @return {@link List} of both {@link Player}s.
     */
    public List<Player> getPlayers() {
        if (state == GameState.WAIT_FOR_PLAYER) {
            return List.of(playerOne);
        }
        return List.of(playerOne, playerTwo);
    }

    /**
     * Removes elements from map by constant threshold.
     */
    private void maintainChangesMap() {
        if (changedFields.size() >= BUFFER_THRESHOLD) {
            for (int i = 0; i < changedFields.size() - 60; i++) {
                changedFields.remove(changedFields.keySet().stream().findFirst().get());// NOSONAR - if statement checks
                // if map contains something
            }
        }
    }

    /**
     * Applies player's move to the game, that is board and then player's
     * statistics.
     * 
     * @param playerId    ID of a player who performed move.
     * @param playersMove information about player's move
     */
    public void applyMove(String playerId, MoveInfo playersMove) {
        getPlayer(playerId).setPointerInfo(new PointerInfo(playersMove.getField(), playersMove.getCursor()));
        getPlayer(playerId).setLastTimeFrame(playersMove.getLastFrame());
        if (state == GameState.IN_PROGRESS) {
            switch (playersMove.getClickType()) {
                case NONE: {
                    maintainChangesMap();
                    return;
                }
                case MIDDLE: {
                    Map<XYcoord<Integer>, BoardFieldInfo> affectedByMiddleFields = board
                            .applyMiddleClickAt(playersMove.getField(), playerId);
                    changedFields.putAll(affectedByMiddleFields);
                    applyRevealMoveToPlayer(affectedByMiddleFields, playerId);
                }
                    break;
                case RIGHT: {
                    Map<XYcoord<Integer>, BoardFieldInfo> affectedByRightFields = board
                            .applyRightClickAt(playersMove.getField(), playerId);
                    changedFields.putAll(affectedByRightFields);
                    applyToPlayerRight(affectedByRightFields, playerId);
                }
                    break;
                case LEFT: {
                    Map<XYcoord<Integer>, BoardFieldInfo> affectedByLeftFields = board
                            .appliesLeftClickAt(playersMove.getField(), playerId);
                    changedFields.putAll(affectedByLeftFields);
                    applyRevealMoveToPlayer(affectedByLeftFields, playerId);
                }
                    break;
            }
        }
    }

    /** Handles flag setting/unsetting */
    private void applyToPlayerRight(Map<XYcoord<Integer>, BoardFieldInfo> affected, String playerId) {
        if (affected.values().stream().anyMatch(BoardField::isFlag)) {
            getPlayer(playerId).addFlag();
        } else {
            getPlayer(playerId).remFlag();
        }
    }

    /** Handles players stats regarding left and middle click - unreaveling move */
    private void applyRevealMoveToPlayer(Map<XYcoord<Integer>, BoardFieldInfo> affected, String playerId) {
        getPlayer(playerId).addRevealedFields(affected.size());
        affected.values().stream().filter(field -> field.getValue().equals(GameBoard.MINE_CODE))
                .forEach(f -> getPlayer(playerId).lifeLost());
        if (getPlayer(playerId).getLives() == 0 || board.areAllMinesCovered()) {
            endTheGame();
        }
    }

    /**
     * Sets game to the end
     */
    public void endTheGame() {
        state = GameState.TERMINATED;
        long playTime = System.currentTimeMillis() - start;
        double timeFactor = TimeUnit.MILLISECONDS.toMinutes(playTime)
                + (double) getSecFromMilis(playTime) / 100;
        playerOne.getPlayerInfo().setScore(calcScore(playerOne.getPlayerInfo(),
                board.getValidFlagsCountForPlayer(playerOne.getId()), timeFactor));
        playerTwo.getPlayerInfo().setScore(calcScore(playerTwo.getPlayerInfo(),
                board.getValidFlagsCountForPlayer(playerTwo.getId()), timeFactor));
        changedFields.putAll(board.getRemainingMines());
        stop = getFrameTime();
        logger.info("Game ended.");
    }

    /** Calculates score in the end of a game wor given player. */
    private double calcScore(PlayerInfo playerInfo, int validFlagsCount, double timeFactor) {
        return (validFlagsCount * playerInfo.getLives() + playerInfo.getRevealedFields() * playerInfo.getLives())
                / timeFactor;
    }

    /** Starts the game. */
    private void startTheGame() {
        state = GameState.IN_PROGRESS;
        changedFields = new HashMap<>();
        start = System.currentTimeMillis();
        board = new GameBoard(start);
        logger.info("Game started.");
    }

    /** Gets seconds from milliseconds */
    private long getSecFromMilis(long playTime) {
        return TimeUnit.MILLISECONDS.toSeconds(playTime) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(playTime));
    }

    /**
     * Gets milliseconds from game start till now, for marking affected fields data
     * with creation time.
     */
    private long getFrameTime() {
        if (state == GameState.TERMINATED) {
            return stop;
        }
        return System.currentTimeMillis() - start;
    }

    /**
     * Gets game state for player with given ID
     * 
     * @param playerId ID of a player.
     * @return {@link BoardStateInfo} with information about current game situation.
     */
    public BoardStateInfo getGameStateForPlayer(String playerId) {
        Player pl = getPlayer(playerId);
        Player op = getOpponent(playerId);
        return buildBoardState(pl, op);
    }

    /**
     * Gets game state for the opponent player with given ID
     * 
     * @param playerId ID of a player.
     * @return {@link BoardStateInfo} with information about current game situation.
     */
    public BoardStateInfo getGameStateForOpponent(String playerId) {
        Player pl = getOpponent(playerId);
        Player op = getPlayer(playerId);
        return buildBoardState(pl, op);
    }

    /** Creates {@ BoardStateInfo} object */
    private BoardStateInfo buildBoardState(Player pl, Player op) {
        return new BoardStateInfo(pl.getPlayerInfo(), op.getPlayerInfo(), op.getPointerInfo(),
                changedFieldsWithOwnership(pl),
                getFrameTime());
    }

    /**
     * Prepares data aobut affected fields for web client, for buffor records newer
     * than given time threshold
     */
    private Map<XYcoord<Integer>, BoardField> changedFieldsWithOwnership(Player player) {
        Map<XYcoord<Integer>, BoardField> returnMap = new HashMap<>();
        Set<Entry<XYcoord<Integer>, BoardFieldInfo>> filteredChanges = changedFields.entrySet().stream()
                .filter(entry -> entry.getValue().getTimeFrame() > player.getLastTimeFrame() - REVTIME)
                .collect(Collectors.toSet());
        for (Entry<XYcoord<Integer>, BoardFieldInfo> entry : filteredChanges) {
            BoardField fieldValue = new BoardField();
            fieldValue.setValue(entry.getValue().getValue());
            fieldValue.setChecked(entry.getValue().isChecked());
            fieldValue.setFlag(entry.getValue().isFlag());
            if (entry.getValue().getOwnerId().equals("")) {
                fieldValue.setOwnerId("ownerId");
            } else if (entry.getValue().getOwnerId().equals(player.getId())) {
                fieldValue.setOwnerId(OWNERSHIP_CODE_PLAYER);
            } else {
                fieldValue.setOwnerId(OWNERSHIP_CODE_OPPONENT);
            }
            returnMap.put(entry.getKey(), fieldValue);
        }
        return returnMap;
    }

    /**
     * Gets player's opponent {@link Player} information for a given player ID.
     * 
     * @param playerId ID of a player
     * @return {@link Player} information.
     */
    public Player getOpponent(String playerId) {
        Predicate<? super Player> predicate = p -> !p.getId().equals(playerId);
        return getPlayerForPredicate(predicate);
    }

    /**
     * Gets player's {@link Player} information for a given player ID.
     * 
     * @param playerId ID of a player
     * @return {@link Player} information.
     */
    public Player getPlayer(String playerId) {
        Predicate<? super Player> predicate = p -> p.getId().equals(playerId);
        return getPlayerForPredicate(predicate);
    }

    /** Gets player filtered by given predicate. */
    private Player getPlayerForPredicate(Predicate<? super Player> predicate) {
        Optional<Player> player = getPlayers().stream().filter(predicate).findFirst();
        if (player.isPresent()) {
            return player.get();
        }
        return null;
    }
}
