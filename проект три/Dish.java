import java.awt.*;

public class Dish {
    private String name;
    private int price;
    private Color color;

    public Dish(String name, int price, Color color) {
        this.name = name;
        this.price = price;
        this.color = color;
    }

    public String getName() { return name; }
    public int getPrice() { return price; }
    public Color getColor() { return color; }
}