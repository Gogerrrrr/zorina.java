public interface ExtendedRestaurantEventListener extends RestaurantEventListener {
    void gameWon(RestaurantEvent e);
    void customerLeftHappy(CustomerEvent e); // НОВОЕ СОБЫТИЕ
}