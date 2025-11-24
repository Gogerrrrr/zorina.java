public class Player extends GameObject {
    private Dish carriedDish;

    public Player(int x, int y) {
        super(x, y);
    }

    public Dish getCarriedDish() { return carriedDish; }
    public void setCarriedDish(Dish carriedDish) { this.carriedDish = carriedDish; }
}