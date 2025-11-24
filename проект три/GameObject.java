import java.awt.*;

public abstract class GameObject {
    protected int x, y;

    public GameObject(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void move(int dx, int dy) {
        x = Math.max(20, Math.min(980, x + dx));
        y = Math.max(20, Math.min(680, y + dy));
    }
}