public interface RestaurantEventListener {
    void customerAdded(CustomerEvent e);
    void customerLeft(CustomerEvent e);
    void orderTaken(OrderEvent e);
    void orderServed(OrderEvent e);
    void wrongOrderServed(WrongOrderEvent e);
    void dishThrownAway(RestaurantEvent e);
}