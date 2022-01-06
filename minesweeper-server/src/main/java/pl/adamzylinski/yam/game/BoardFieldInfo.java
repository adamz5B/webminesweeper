package pl.adamzylinski.yam.game;

/**
 * Extends {@link BoardField} to the form necessary for changes buffer.
 */
public class BoardFieldInfo extends BoardField {
    private long timeFrame = 0;

    public BoardFieldInfo(long start) {
        super();
        timeFrame = System.currentTimeMillis() - start;
    }

    public void setTimeFrame(long timeFrame) {
        this.timeFrame = timeFrame;
    }

    public long getTimeFrame() {
        return timeFrame;
    }
}
