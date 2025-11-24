public class CustomerEvent extends RestaurantEvent {
    private Customer customer;
    public CustomerEvent(Object source, Customer customer) {
        super(source);
        this.customer = customer;
    }
    public Customer getCustomer() { return customer; }
}