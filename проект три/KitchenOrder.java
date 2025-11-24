public class KitchenOrder {
    private Dish dish;
    private String source; // "клиент" или "официант"

    public KitchenOrder(Dish dish, String source) {
        this.dish = dish;
        this.source = source;
    }

    public Dish getDish() {
        return dish;
    }

    public String getSource() {
        return source;
    }
}