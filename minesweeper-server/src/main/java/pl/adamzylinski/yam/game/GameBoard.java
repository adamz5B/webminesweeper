package pl.adamzylinski.yam.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import pl.adamzylinski.yam.models.XYcoord;

/**
 * Class for handling game board situation. It provides methods to change and
 * retrieve state of the board during a game.
 */
public class GameBoard {
    public static final String MINE_CODE = "m";

    // Constants in this class has configuration role and should be moved to some
    // kind of properties later
    private static final int BOARD_WIDTH = 40;
    private static final int BOARD_HEIGHT = 30;
    private static final double MINES_RATIO = 0.3;

    private BoardField[][] board = new BoardField[BOARD_HEIGHT][BOARD_WIDTH];

    private int minesNo;
    private long started;

    /**
     * Game board class constructor.
     * 
     * @param started long timestamp of the game start
     */
    public GameBoard(long started) {
        this.started = started;
        initBoard();
        setMines();
    }

    /**
     * Initializes game board with {@link BoardField} values
     */
    private void initBoard() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] = new BoardField();
            }
        }
    }

    /**
     * Applies middle click to a given coordinates.
     * 
     * @param fieldXyCoords a {@link XYcoord} click location
     * @param playerId      player ID used in ownership marking.
     * @return {@link Map} containing affected fields with {@link XYcoord} as key
     *         and {@link BoardFieldInfo} as value.
     */
    public Map<XYcoord<Integer>, BoardFieldInfo> applyMiddleClickAt(XYcoord<Integer> fieldXyCoords, String playerId) {
        return checkedChecker(fieldXyCoords, playerId);
    }

    /**
     * Since middle click consists on checking neighbours of a already checked
     * field, this method checks if mine count indicated by field is equal to set
     * flag/detenated mines, and then calls function for standard "click" an a
     * remaining neighbour fields.
     */
    private Map<XYcoord<Integer>, BoardFieldInfo> checkedChecker(XYcoord<Integer> fieldXyCoords, String playerId) {
        int x = fieldXyCoords.getX();
        int y = fieldXyCoords.getY();
        Map<XYcoord<Integer>, BoardFieldInfo> changes = new HashMap<>();
        if (board[y][x].isChecked() && board[y][x].getValue().equals(calcFlagsNMines(fieldXyCoords))) {
            for (int j = -1; j < 2; j++) {
                int rj = y + j;
                if (rj < 0 || rj > (BOARD_HEIGHT - 1)) {
                    continue;
                }
                for (int i = -1; i < 2; i++) {
                    int ci = x + i;
                    if (ci < 0 || ci > (BOARD_WIDTH - 1) || (ci == x && rj == y)) {
                        continue;
                    }
                    changes.putAll(checkField(new XYcoord<>(ci, rj), playerId));
                }
            }
        }
        return changes;
    }

    /**
     * Calculates number of flags and detonated mines surrounding given field
     * coordinates.
     */
    private String calcFlagsNMines(XYcoord<Integer> fieldXyCoords) {
        int x = fieldXyCoords.getX();
        int y = fieldXyCoords.getY();
        Integer counter = 0;
        for (int j = -1; j < 2; j++) {
            int rj = y + j;
            if (rj < 0 || rj > (BOARD_HEIGHT - 1)) {
                continue;
            }
            for (int i = -1; i < 2; i++) {
                int ci = x + i;
                if (ci < 0 || ci > (BOARD_WIDTH - 1) || (ci == x && rj == y)) {
                    continue;
                }
                if (board[rj][ci].isFlag()
                        || (board[rj][ci].isChecked() && board[rj][ci].getValue().equals(MINE_CODE))) {
                    counter++;
                }
            }
        }
        return counter.toString();
    }

    /**
     * Checks content of unchecked field, without a flag. If field is zero, calls
     * function to check neighbour fields.
     */
    private Map<XYcoord<Integer>, BoardFieldInfo> checkField(XYcoord<Integer> fieldXyCoords, String playerId) {
        int x = fieldXyCoords.getX();
        int y = fieldXyCoords.getY();
        if (board[y][x].isFlag() || board[y][x].isChecked()) {
            return Collections.emptyMap();
        }
        board[y][x].setChecked(true);
        BoardFieldInfo ret = new BoardFieldInfo(started);
        ret.setOwnerId(playerId);
        ret.setChecked(true);
        if (board[y][x].getValue().equals(MINE_CODE)) {
            ret.setValue(MINE_CODE);
            return Map.of(fieldXyCoords, ret);
        } else {
            board[y][x].setValue(calcNeighbours(fieldXyCoords));
            ret.setValue(board[y][x].getValue());
        }
        Map<XYcoord<Integer>, BoardFieldInfo> changes = new HashMap<>();
        changes.put(fieldXyCoords, ret);
        if (board[y][x].getValue().equals("0")) {
            changes.putAll(revealZeroNeighbours(fieldXyCoords, playerId));
        }
        return changes;
    }

    /**
     * Gets number (as a string) of mines in surrounding fields.
     */
    private String calcNeighbours(XYcoord<Integer> fieldXyCoords) {
        int x = fieldXyCoords.getX();
        int y = fieldXyCoords.getY();
        Integer counter = 0;
        for (int j = -1; j < 2; j++) {
            int rj = y + j;
            if (rj < 0 || rj > (BOARD_HEIGHT - 1)) {
                continue;
            }
            for (int i = -1; i < 2; i++) {
                int ci = x + i;
                if (ci < 0 || ci > (BOARD_WIDTH - 1) || (ci == x && rj == y)) {
                    continue;
                }
                if (board[rj][ci].getValue().equals(MINE_CODE)) {
                    counter++;
                }
            }
        }
        return counter.toString();
    }

    /**
     * Tries to reveal cells next to a cell with zero neighbour mines.
     */
    private Map<XYcoord<Integer>, BoardFieldInfo> revealZeroNeighbours(XYcoord<Integer> fieldXyCoords,
            String playerId) {
        int x = fieldXyCoords.getX();
        int y = fieldXyCoords.getY();
        Map<XYcoord<Integer>, BoardFieldInfo> changes = new HashMap<>();
        for (int j = -1; j < 2; j++) {
            int rj = y + j;
            if (rj < 0 || rj > (BOARD_HEIGHT - 1)) {
                continue;
            }
            for (int i = -1; i < 2; i++) {
                int ci = x + i;
                if (ci < 0 || ci > (BOARD_WIDTH - 1) || (ci == x && rj == y) || board[rj][ci].isChecked()
                        || board[rj][ci].getValue().equals(MINE_CODE)) {
                    continue;
                }
                String counter = this.calcNeighbours(new XYcoord<>(ci, rj));
                if (counter.equals("0") && !board[rj][ci].isChecked()) {
                    board[rj][ci].setChecked(true);
                    changes.putAll(revealZeroNeighbours(new XYcoord<>(ci, rj), playerId));
                } else {
                    board[rj][ci].setChecked(true);
                    board[rj][ci].setValue(counter);
                }
                BoardFieldInfo ret = new BoardFieldInfo(started);
                ret.setValue(board[rj][ci].getValue());
                ret.setChecked(board[rj][ci].isChecked());
                ret.setOwnerId(playerId);
                changes.put(new XYcoord<>(ci, rj), ret);
            }
        }
        return changes;
    }

    /**
     * Applies right click, which toggles flag for a player at given field.
     * 
     * @param fieldXyCoords a {@link XYcoord} click location
     * @param playerId      player ID used in ownership marking.
     * @return {@link Map} containing affected fields with {@link XYcoord} as key
     *         and {@link BoardFieldInfo} as value.
     */
    public Map<XYcoord<Integer>, BoardFieldInfo> applyRightClickAt(XYcoord<Integer> fieldXyCoords, String playerId) {
        Integer x = fieldXyCoords.getX();
        Integer y = fieldXyCoords.getY();
        if (board[y][x].isChecked()) {
            return Collections.emptyMap();
        }
        if (board[y][x].isFlag() && board[y][x].getOwnerId().equals(playerId)) {
            board[y][x]
                    .setFlag(false);
            board[y][x].setOwnerId("");
        } else if (board[y][x].isFlag() && !board[y][x].getOwnerId().equals(playerId)) {
            return Collections.emptyMap();
        } else {
            board[y][x]
                    .setFlag(true);
            board[y][x].setOwnerId(playerId);
        }
        BoardFieldInfo ret = new BoardFieldInfo(started);
        ret.setFlag(board[y][x].isFlag());
        ret.setOwnerId(board[y][x].isFlag() ? playerId : "");
        return Map.of(fieldXyCoords, ret);
    }

    /**
     * Applies left click on a field, which reveals field's content.
     * 
     * @param fieldXyCoords a {@link XYcoord} click location
     * @param playerId      player ID used in ownership marking.
     * @return {@link Map} containing affected fields with {@link XYcoord} as key
     *         and {@link BoardFieldInfo} as value.
     */
    public Map<XYcoord<Integer>, BoardFieldInfo> appliesLeftClickAt(XYcoord<Integer> fieldXyCoords, String playerId) {
        return checkField(fieldXyCoords, playerId);
    }

    /**
     * Sets mines on board, mines number is always a percentage of a map size.
     */
    private void setMines() {
        List<XYcoord<Integer>> coords = new ArrayList<>();
        int minesCount = (int) Math.floor((double) (BOARD_HEIGHT * BOARD_HEIGHT) * MINES_RATIO);// NOSONAR
        minesNo = minesCount;
        Random r = new Random(System.currentTimeMillis());
        while (minesCount > 0) {
            int x = r.nextInt(BOARD_WIDTH);
            int y = r.nextInt(BOARD_HEIGHT);
            if (coords.stream().noneMatch(s -> s.getX() == x && s.getY() == y)) {
                XYcoord<Integer> mineXY = new XYcoord<>(x, y);
                coords.add(mineXY);
                board[mineXY.getY()][mineXY.getX()].setValue(MINE_CODE);
                minesCount--;
            }
        }
    }

    /**
     * Checks if all mines are discovered/marked.
     * 
     * @return true i ale mines are discovered/marked.
     */
    public boolean areAllMinesCovered() {
        int counter = 0;
        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[y].length; x++) {
                if (board[y][x].getValue().equals(MINE_CODE) && (board[y][x].isChecked() || board[y][x].isFlag())) {
                    counter++;
                }
            }
        }
        return minesNo == counter;
    }

    /**
     * Counts number of flags placed by player on mines.
     * 
     * @param playerId player ID used in ownership marking.
     * @return number of correctly marked fields.
     */
    public int getValidFlagsCountForPlayer(String playerId) {
        int counter = 0;
        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[y].length; x++) {
                if (board[y][x].getValue().equals(MINE_CODE) && board[y][x].isFlag()
                        && board[y][x].getOwnerId().equals(playerId)) {
                    counter++;
                }
            }
        }
        return counter;
    }

    /**
     * Gets undiscovered fields with mines.
     * 
     * @return{@link Map} containing fields with mines, with {@link XYcoord} as key
     *               and {@link BoardFieldInfo} as value.
     */
    public Map<XYcoord<Integer>, BoardFieldInfo> getRemainingMines() {
        Map<XYcoord<Integer>, BoardFieldInfo> content = new HashMap<>();
        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[y].length; x++) {
                if (board[y][x].getValue().equals(MINE_CODE) && !board[y][x].isFlag()
                        && !board[y][x].isChecked()) {
                    BoardFieldInfo ret = new BoardFieldInfo(started);
                    ret.setValue(MINE_CODE);
                    ret.setChecked(true);
                    content.put(new XYcoord<>(x, y), ret);
                }
            }
        }
        return content;
    }
}
