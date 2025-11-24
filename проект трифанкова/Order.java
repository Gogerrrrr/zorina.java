public class Order {
    private Customer customer;
    private Dish dish;

    public Order(Customer customer, Dish dish) {
        this.customer = customer;
        this.dish = dish;
    }

    public Customer getCustomer() { return customer; }
    public Dish getDish() { return dish; }
}