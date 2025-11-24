import java.awt.*;

public class Customer extends GameObject {
    private Color color;
    private int patience;
    private int maxPatience;
    private Dish desiredDish;
    private Order currentOrder;
    private boolean served;

    public Customer(int id) {
        super(200 + (id % 3) * 200, 150 + (id / 3) * 180);
        this.color = getRandomClothingColor();
        this.maxPatience = 600; // УВЕЛИЧИЛ в 2 раза (было 300)
        this.patience = maxPatience;
        this.served = false;
    }

    private Color getRandomClothingColor() {
        Color[] clothingColors = {
                new Color(65, 105, 225),   // Royal Blue
                new Color(220, 20, 60),    // Crimson
                new Color(46, 139, 87),    // Sea Green
                new Color(148, 0, 211),    // Dark Violet
                new Color(255, 140, 0),    // Dark Orange
                new Color(178, 34, 34),    // Firebrick
                new Color(30, 144, 255),   // Dodger Blue
                new Color(50, 205, 50)     // Lime Green
        };
        return clothingColors[(int)(Math.random() * clothingColors.length)];
    }

    public void updatePatience() {
        if (patience > 0 && !served) {
            patience -= 1; // Уменьшаем медленнее
        }
    }

    public Color getColor() { return color; }
    public int getPatience() { return patience; }
    public int getMaxPatience() { return maxPatience; }
    public Dish getDesiredDish() { return desiredDish; }
    public void setDesiredDish(Dish desiredDish) { this.desiredDish = desiredDish; }
    public Order getCurrentOrder() { return currentOrder; }
    public void setCurrentOrder(Order currentOrder) { this.currentOrder = currentOrder; }
    public boolean isServed() { return served; }
    public void setServed(boolean served) { this.served = served; }
}