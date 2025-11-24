public class OrderEvent extends RestaurantEvent {
    private Order order;
    public OrderEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }
    public Order getOrder() { return order; }
}