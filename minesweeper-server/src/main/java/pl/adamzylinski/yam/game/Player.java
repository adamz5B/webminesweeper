package pl.adamzylinski.yam.game;

import pl.adamzylinski.yam.models.PlayerInfo;
import pl.adamzylinski.yam.models.PointerInfo;

/**
 * Represents player of the game.
 */
public class Player {
    private String id;
    private PointerInfo pointerInfo;
    private PlayerInfo playerInfo;
    private long lastTimeFrame;

    public Player(String id) {
        this.id = id;
        pointerInfo = new PointerInfo();
        playerInfo = new PlayerInfo();
    }

    public String getId() {
        return id;
    }

    /**
     * Trigger decrease of players lives
     */
    public void lifeLost() {
        playerInfo.lifeLost();
    }

    public int getLives() {
        return playerInfo.getLives();
    }

    /**
     * Adds to player's set flag counter.
     */
    public void addFlag() {
        playerInfo.addFlag();
    }

    /**
     * Decreses player's set flag counter.
     */
    public void remFlag() {
        playerInfo.remFlag();
    }

    public int getFlags() {
        return playerInfo.getFlags();
    }

    /**
     * Adds to number of fields revealed by player.
     * 
     * @param addedFieldsNumber
     */
    public void addRevealedFields(int addedFieldsNumber) {
        playerInfo.addRevealedFields(addedFieldsNumber);
    }

    /**
     * Gets number of fields revealed by player.
     * 
     * @return number of fields revealed by player.
     */
    public int getRevealedFields() {
        return playerInfo.getRevealedFields();
    }

    public PlayerInfo getPlayerInfo() {
        return playerInfo;
    }

    public void setPointerInfo(PointerInfo pointerInfo) {
        this.pointerInfo = pointerInfo;
    }

    public PointerInfo getPointerInfo() {
        return pointerInfo;
    }

    public void setLastTimeFrame(long lastTimeFrame) {
        this.lastTimeFrame = lastTimeFrame;
    }

    public long getLastTimeFrame() {
        return lastTimeFrame;
    }
}
