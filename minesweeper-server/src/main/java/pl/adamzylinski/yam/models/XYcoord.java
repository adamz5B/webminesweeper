package pl.adamzylinski.yam.models;

/**
 * Class for representing 2D coordinates
 */
public class XYcoord<T> {
    private T x;
    private T y;

    public XYcoord(T x, T y) {
        this.x = x;
        this.y = y;
    }

    public XYcoord(XYcoord<T> xyCoord) {
        this.x = xyCoord.getX();
        this.y = xyCoord.getY();
    }

    public void setX(T x) {
        this.x = x;
    }

    public T getX() {
        return x;
    }

    public void setY(T y) {
        this.y = y;
    }

    public T getY() {
        return y;
    }
}
