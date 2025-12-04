import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.awt.Color;
import javax.swing.Timer;

public class RestaurantModel {
    private List<Customer> customers;
    private List<Table> tables;
    private Player player;
    private int score;
    private int money;
    private boolean gameRunning;

    private List<Dish> menu;
    private List<Dish> kitchenOrders; // Готовые блюда
    private List<Dish> kitchenQueue; // Блюда в процессе приготовления
    private Map<Dish, Integer> cookingProgress;
    private Map<Customer, Timer> customerLeaveTimers;

    private int trashBinX = 900;
    private int trashBinY = 100;

    public RestaurantModel() {
        customers = new ArrayList<>();
        tables = new ArrayList<>();
        menu = new ArrayList<>();
        kitchenOrders = new ArrayList<>();
        kitchenQueue = new ArrayList<>();
        cookingProgress = new HashMap<>();
        customerLeaveTimers = new HashMap<>();
        player = new Player(400, 300);
        score = 0;
        money = 100;
        gameRunning = true;

        initializeTables();
        initializeMenu();
    }

    private void initializeTables() {
        for (int i = 0; i < 6; i++) {
            int x = 200 + (i % 3) * 200;
            int y = 150 + (i / 3) * 180;
            tables.add(new Table(x, y, i));
        }
    }

    private void initializeMenu() {
        menu.add(new Dish("Пицца", 50, new Color(220, 20, 60)));
        menu.add(new Dish("Бургер", 40, new Color(210, 105, 30)));
        menu.add(new Dish("Салат", 30, new Color(50, 205, 50)));
        menu.add(new Dish("Суши", 60, new Color(255, 182, 193)));
        menu.add(new Dish("Паста", 45, new Color(255, 215, 0)));
        menu.add(new Dish("Суп", 35, new Color(139, 69, 19)));
    }

    public void addCustomer() {
        if (customers.size() < 6) {
            Customer customer = new Customer(customers.size());
            Dish desiredDish = menu.get((int)(Math.random() * menu.size()));
            customer.setDesiredDish(desiredDish);
            customers.add(customer);
            fireCustomerAdded(customer);
        }
    }

    public void updateCustomers() {
        Iterator<Customer> iterator = customers.iterator();
        while (iterator.hasNext()) {
            Customer customer = iterator.next();
            customer.updatePatience();

            if (customer.getPatience() <= 0) {
                money -= 25;
                score -= 10;
                iterator.remove();
                if (customerLeaveTimers.containsKey(customer)) {
                    customerLeaveTimers.get(customer).stop();
                    customerLeaveTimers.remove(customer);
                }
                fireCustomerLeft(customer);
            }
        }
        updateCookingProgress();
    }

    private void updateCookingProgress() {
        // Создаем копию для безопасного удаления
        List<Dish> queueCopy = new ArrayList<>(kitchenQueue);

        for (Dish dish : queueCopy) {
            int progress = cookingProgress.getOrDefault(dish, 0) + 10;
            cookingProgress.put(dish, progress);

            System.out.println("🔄 Готовим: " + dish.getName() + " - " + progress + "%");

            if (progress >= 100) {
                // Блюдо готово!
                kitchenOrders.add(dish);
                kitchenQueue.remove(dish);
                cookingProgress.remove(dish);
                fireOrderReady(dish);
                System.out.println("✅ Блюдо готово: " + dish.getName());
            }
        }
    }

    // Новый метод для сохранения заказа в БД
    private void saveOrderToDatabase(String dishName, int price, Integer customerId, String status) {
        GameRepository.saveOrder(dishName, price, customerId, status);
    }

    // ПРОСТОЙ И НАДЕЖНЫЙ МЕТОД ДЛЯ ЗАКАЗА БЛЮДА
    public boolean placeOrderInKitchen(Dish dish) {
        System.out.println("🎯 НАЧАЛО: Пытаемся заказать " + dish.getName());

        // Проверяем деньги
        if (money < dish.getPrice()) {
            System.out.println("❌ Недостаточно денег для заказа " + dish.getName());
            return false;
        }

        // Проверяем очередь
        if (kitchenQueue.size() >= 10) {
            System.out.println("❌ Очередь кухни переполнена");
            return false;
        }

        // Снимаем деньги
        money -= dish.getPrice();
        score += 5;

        // Добавляем в очередь
        kitchenQueue.add(dish);
        cookingProgress.put(dish, 0);

        // Сохраняем заказ в БД (заказ официанта)
        saveOrderToDatabase(dish.getName(), dish.getPrice(), null, "заказано_официантом");

        System.out.println("✅ УСПЕХ: Заказали " + dish.getName() + " за " + dish.getPrice() + "$");
        System.out.println("📊 Очередь кухни: " + kitchenQueue.size() + " блюд");

        // Выводим всю очередь для отладки
        for (int i = 0; i < kitchenQueue.size(); i++) {
            System.out.println("   " + (i+1) + ". " + kitchenQueue.get(i).getName());
        }

        fireOrderPlacedByWaiter(dish);
        return true;
    }

    // МЕТОД ДЛЯ ПРИНЯТИЯ ЗАКАЗА ОТ КЛИЕНТА (бесплатно)
    public boolean takeOrderFromCustomer(Customer customer) {
        if (customer.getDesiredDish() != null && customer.getCurrentOrder() == null) {
            Dish desiredDish = customer.getDesiredDish();

            // Проверяем очередь
            if (kitchenQueue.size() >= 10) {
                System.out.println("❌ Очередь кухни переполнена для заказа клиента");
                return false;
            }

            // Создаем заказ
            Order order = new Order(customer, desiredDish);
            customer.setCurrentOrder(order);

            // Добавляем в очередь (бесплатно для клиента)
            kitchenQueue.add(desiredDish);
            cookingProgress.put(desiredDish, 0);

            // Сохраняем заказ в БД (заказ клиента)
            saveOrderToDatabase(desiredDish.getName(), desiredDish.getPrice(),
                    customer.hashCode(), "заказано_клиентом");

            System.out.println("📝 Принят заказ от клиента: " + desiredDish.getName());
            System.out.println("📊 Очередь кухни: " + kitchenQueue.size() + " блюд");

            fireOrderTaken(order);
            return true;
        }
        return false;
    }

    public void takeOrderFromKitchen() {
        if (!kitchenOrders.isEmpty() && player.getCarriedDish() == null) {
            Dish dish = kitchenOrders.remove(0);
            player.setCarriedDish(dish);
            System.out.println("📦 Взято с кухни: " + dish.getName());
        }
    }

    public boolean takeSpecificDishFromKitchen(Dish desiredDish) {
        if (player.getCarriedDish() == null) {
            for (Dish dish : kitchenOrders) {
                if (dish.getName().equals(desiredDish.getName())) {
                    player.setCarriedDish(dish);
                    kitchenOrders.remove(dish);
                    System.out.println("📦 Взято конкретное блюдо: " + dish.getName());
                    return true;
                }
            }
        }
        return false;
    }

    public boolean serveCustomer(Customer customer) {
        if (player.getCarriedDish() == null) {
            System.out.println("❌ У официанта нет блюда");
            return false;
        }

        Dish carriedDish = player.getCarriedDish();

        if (customer.getCurrentOrder() != null) {
            if (carriedDish.getName().equals(customer.getCurrentOrder().getDish().getName())) {
                // Правильный заказ!
                int price = customer.getCurrentOrder().getDish().getPrice();
                money += price;
                score += 15;
                player.setCarriedDish(null);
                customer.setServed(true);
                Order servedOrder = customer.getCurrentOrder();
                customer.setCurrentOrder(null);
                customer.setDesiredDish(null);

                fireOrderServed(new OrderEvent(this, servedOrder));
                System.out.println("🎉 Правильный заказ! +" + price + "$, +15 очков");

                scheduleCustomerLeave(customer, 3000);
                checkWinCondition();
                return true;
            } else {
                // Неправильный заказ
                money -= 15;
                score -= 8;
                player.setCarriedDish(null);
                fireWrongOrderServed(new WrongOrderEvent(this, customer, carriedDish));
                System.out.println("❌ Неправильный заказ! Штраф -15$, -8 очков");
                checkGameOver();
                return false;
            }
        } else {
            System.out.println("❌ Клиент не ожидает заказ");
            return false;
        }
    }

    public boolean throwAwayDishInTrashBin() {
        if (player.getCarriedDish() != null) {
            Dish thrownDish = player.getCarriedDish();
            player.setCarriedDish(null);
            money -= 8;
            score -= 5;
            fireDishThrownAway(new RestaurantEvent(this));
            System.out.println("🗑️ Блюдо выброшено в мусорку: " + thrownDish.getName() + "! Штраф -8$, -5 очков");
            checkGameOver();
            return true;
        }
        return false;
    }

    public void throwAwayDish() {
        if (player.getCarriedDish() != null) {
            Dish thrownDish = player.getCarriedDish();
            player.setCarriedDish(null);
            score -= 3;
            fireDishThrownAway(new RestaurantEvent(this));
            System.out.println("🗑️ Блюдо выброшено: " + thrownDish.getName() + "! Штраф -3 очка");
        }
    }

    private void checkGameOver() {
        if (money <= 0 || score < -50) {
            gameRunning = false;
            fireGameOver();
        }
    }

    private void scheduleCustomerLeave(Customer customer, int delay) {
        if (customerLeaveTimers.containsKey(customer)) {
            customerLeaveTimers.get(customer).stop();
        }

        Timer leaveTimer = new Timer(delay, e -> {
            removeCustomer(customer);
            fireCustomerLeftHappy(customer);
        });
        leaveTimer.setRepeats(false);
        leaveTimer.start();

        customerLeaveTimers.put(customer, leaveTimer);
    }

    private void removeCustomer(Customer customer) {
        customers.remove(customer);
        customerLeaveTimers.remove(customer);
        System.out.println("🚪 Клиент ушел довольный");
    }

    private void checkWinCondition() {
        if (score >= 100) {
            gameRunning = false;
            fireGameWon();
        }
    }

    public void cleanupTable(Table table) {
        table.setClean(true);
        score += 5;
        checkWinCondition();
    }

    public int getCookingProgress(Dish dish) {
        return cookingProgress.getOrDefault(dish, 0);
    }

    public int getTrashBinX() { return trashBinX; }
    public int getTrashBinY() { return trashBinY; }

    private List<RestaurantEventListener> listeners = new ArrayList<>();

    public void addRestaurantEventListener(RestaurantEventListener listener) {
        listeners.add(listener);
    }

    private void fireCustomerAdded(Customer customer) {
        for (RestaurantEventListener listener : listeners) {
            listener.customerAdded(new CustomerEvent(this, customer));
        }
    }

    private void fireCustomerLeft(Customer customer) {
        for (RestaurantEventListener listener : listeners) {
            listener.customerLeft(new CustomerEvent(this, customer));
        }
    }

    private void fireCustomerLeftHappy(Customer customer) {
        for (RestaurantEventListener listener : listeners) {
            if (listener instanceof ExtendedRestaurantEventListener) {
                ((ExtendedRestaurantEventListener) listener).customerLeftHappy(new CustomerEvent(this, customer));
            }
        }
    }

    private void fireGameOver() {
        for (RestaurantEventListener listener : listeners) {
            if (listener instanceof ExtendedRestaurantEventListener) {
                ((ExtendedRestaurantEventListener) listener).gameOver(new RestaurantEvent(this));
            }
        }
    }

    private void fireOrderTaken(Order order) {
        for (RestaurantEventListener listener : listeners) {
            listener.orderTaken(new OrderEvent(this, order));
        }
    }

    private void fireOrderServed(OrderEvent orderEvent) {
        for (RestaurantEventListener listener : listeners) {
            listener.orderServed(orderEvent);
        }
    }

    private void fireWrongOrderServed(WrongOrderEvent event) {
        for (RestaurantEventListener listener : listeners) {
            listener.wrongOrderServed(event);
        }
    }

    private void fireDishThrownAway(RestaurantEvent event) {
        for (RestaurantEventListener listener : listeners) {
            listener.dishThrownAway(event);
        }
    }

    private void fireOrderReady(Dish dish) {
        System.out.println("🔥 СОБЫТИЕ: Блюдо готово - " + dish.getName());
        for (RestaurantEventListener listener : listeners) {
            listener.orderTaken(new OrderEvent(this, new Order(null, dish)));
        }
    }

    private void fireOrderPlacedByWaiter(Dish dish) {
        System.out.println("👨‍🍳 СОБЫТИЕ: Официант заказал - " + dish.getName());
        for (RestaurantEventListener listener : listeners) {
            listener.orderTaken(new OrderEvent(this, new Order(null, dish)));
        }
    }

    private void fireGameWon() {
        for (RestaurantEventListener listener : listeners) {
            if (listener instanceof ExtendedRestaurantEventListener) {
                ((ExtendedRestaurantEventListener) listener).gameWon(new RestaurantEvent(this));
            }
        }
    }

    // Геттеры
    public List<Customer> getCustomers() { return customers; }
    public List<Table> getTables() { return tables; }
    public Player getPlayer() { return player; }
    public int getScore() { return score; }
    public int getMoney() { return money; }
    public boolean isGameRunning() { return gameRunning; }
    public List<Dish> getKitchenOrders() { return kitchenOrders; }
    public List<Dish> getKitchenQueue() { return kitchenQueue; }
    public List<Dish> getMenu() { return menu; }
}