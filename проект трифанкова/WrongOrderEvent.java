public class WrongOrderEvent extends RestaurantEvent {
    private Customer customer;
    private Dish wrongDish;
    public WrongOrderEvent(Object source, Customer customer, Dish wrongDish) {
        super(source);
        this.customer = customer;
        this.wrongDish = wrongDish;
    }
    public Customer getCustomer() { return customer; }
    public Dish getWrongDish() { return wrongDish; }
}