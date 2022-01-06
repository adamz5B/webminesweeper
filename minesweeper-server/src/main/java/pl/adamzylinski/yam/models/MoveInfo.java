package pl.adamzylinski.yam.models;

/** Information about player's move (from web client) */
public class MoveInfo implements GameMessageI {
    private XYcoord<Integer> field;
    private XYcoord<Integer> cursor;
    private ClickType clickType;
    private Long lastFrame;

    public MoveInfo(int fieldX, int fieldY, int cursorX, int cursorY, ClickType clickType, long lastFrame) {
        this(new XYcoord<>(fieldX, fieldY), new XYcoord<>(cursorX, cursorY), clickType, lastFrame);
    }

    public MoveInfo(XYcoord<Integer> field, XYcoord<Integer> cursor, ClickType cliskType, long lastFrame) {
        this.field = field;
        this.cursor = cursor;
        this.clickType = cliskType;
        this.lastFrame = lastFrame;
    }

    public XYcoord<Integer> getField() {
        return field;
    }

    public void setField(XYcoord<Integer> field) {
        this.field = field;
    }

    public XYcoord<Integer> getCursor() {
        return cursor;
    }

    public void setCursor(XYcoord<Integer> cursor) {
        this.cursor = cursor;
    }

    public void setClickType(ClickType clickType) {
        this.clickType = clickType;
    }

    public ClickType getClickType() {
        return clickType;
    }

    public void setLastFrame(Long lastFrame) {
        this.lastFrame = lastFrame;
    }

    public Long getLastFrame() {
        return lastFrame;
    }
}
