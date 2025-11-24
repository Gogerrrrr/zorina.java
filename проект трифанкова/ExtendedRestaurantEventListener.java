public interface ExtendedRestaurantEventListener extends RestaurantEventListener {
    void gameWon(RestaurantEvent e);
    void customerLeftHappy(CustomerEvent e);
    void gameOver(RestaurantEvent e); // НОВОЕ СОБЫТИЕ ПРОИГРЫША
}