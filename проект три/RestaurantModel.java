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

    private Map<Color, Integer> customerColors;
    private List<Order> activeOrders;
    private List<Dish> menu;
    private List<Dish> kitchenOrders;
    private List<Dish> kitchenQueue;
    private Map<Dish, Integer> cookingProgress;
    private Map<Customer, Timer> customerLeaveTimers; // Таймеры для ухода клиентов

    public RestaurantModel() {
        customers = new ArrayList<>();
        tables = new ArrayList<>();
        customerColors = new HashMap<>();
        activeOrders = new ArrayList<>();
        kitchenOrders = new ArrayList<>();
        kitchenQueue = new ArrayList<>();
        cookingProgress = new HashMap<>();
        customerLeaveTimers = new HashMap<>();
        player = new Player(400, 300);
        score = 0;
        money = 100;
        gameRunning = true;

        initializeTables();
        initializeCustomerColors();
        initializeMenu();
    }

    private void initializeTables() {
        for (int i = 0; i < 6; i++) {
            int x = 200 + (i % 3) * 200;
            int y = 150 + (i / 3) * 180;
            tables.add(new Table(x, y, i));
        }
    }

    private void initializeCustomerColors() {
        customerColors.put(Color.BLUE, 1);
        customerColors.put(Color.RED, 2);
        customerColors.put(Color.GREEN, 3);
        customerColors.put(Color.MAGENTA, 4);
    }

    private void initializeMenu() {
        menu = new ArrayList<>();
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
                money -= 20;
                iterator.remove();
                // Останавливаем таймер если клиент ушел из-за нетерпения
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
        List<Dish> dishesToRemove = new ArrayList<>();

        for (Dish dish : kitchenQueue) {
            int progress = cookingProgress.getOrDefault(dish, 0) + 10;
            cookingProgress.put(dish, progress);

            if (progress >= 100) {
                kitchenOrders.add(dish);
                dishesToRemove.add(dish);
                fireOrderReady(dish);
                System.out.println("✅ Блюдо готово: " + dish.getName());
            }
        }

        for (Dish dish : dishesToRemove) {
            kitchenQueue.remove(dish);
            cookingProgress.remove(dish);
        }
    }

    // МЕТОД ДЛЯ ПРИНЯТИЯ ЗАКАЗА ОТ КЛИЕНТА
    public boolean takeOrderFromCustomer(Customer customer) {
        if (customer.getDesiredDish() != null && customer.getCurrentOrder() == null) {
            Order order = new Order(customer, customer.getDesiredDish());
            customer.setCurrentOrder(order);
            addDishToKitchenQueue(customer.getDesiredDish());
            activeOrders.add(order);
            fireOrderTaken(order);
            System.out.println("📝 Принят заказ: " + customer.getDesiredDish().getName());
            return true;
        }
        return false;
    }

    public boolean placeOrderInKitchen(Dish dish) {
        int dishPrice = dish.getPrice();
        if (money >= dishPrice) {
            money -= dishPrice;
            score += 5;
            addDishToKitchenQueue(dish);
            fireOrderPlacedByWaiter(dish);
            return true;
        }
        return false;
    }

    private void addDishToKitchenQueue(Dish dish) {
        kitchenQueue.add(dish);
        cookingProgress.put(dish, 0);
        System.out.println("🎯 Добавлено в очередь: " + dish.getName());
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

    // ОСНОВНОЙ МЕТОД ДЛЯ ОТДАЧИ ЗАКАЗА КЛИЕНТУ
    public boolean serveCustomer(Customer customer) {
        if (player.getCarriedDish() == null) {
            System.out.println("❌ У официанта нет блюда");
            return false;
        }

        Dish carriedDish = player.getCarriedDish();

        // Если у клиента есть заказ, проверяем совпадение
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

                // ЗАПУСКАЕМ ТАЙМЕР ДЛЯ УХОДА КЛИЕНТА
                scheduleCustomerLeave(customer, 3000); // Уйдет через 3 секунды

                checkWinCondition();
                return true;
            } else {
                // Неправильный заказ
                money -= 10;
                score -= 5;
                player.setCarriedDish(null);
                fireWrongOrderServed(new WrongOrderEvent(this, customer, carriedDish));
                System.out.println("❌ Неправильный заказ! Штраф -10$");
                return false;
            }
        } else if (customer.getDesiredDish() != null && customer.getCurrentOrder() == null) {
            // Клиент еще не сделал заказ, но хочет что-то
            if (carriedDish.getName().equals(customer.getDesiredDish().getName())) {
                // Клиент получает то, что хотел
                int price = customer.getDesiredDish().getPrice();
                money += price;
                score += 10;
                player.setCarriedDish(null);
                customer.setServed(true);
                customer.setDesiredDish(null);

                fireOrderServed(new OrderEvent(this, new Order(customer, carriedDish)));
                System.out.println("🎉 Клиент доволен! +" + price + "$, +10 очков");

                // ЗАПУСКАЕМ ТАЙМЕР ДЛЯ УХОДА КЛИЕНТА
                scheduleCustomerLeave(customer, 3000); // Уйдет через 3 секунды

                checkWinCondition();
                return true;
            } else {
                // Не то блюдо
                money -= 5;
                score -= 3;
                player.setCarriedDish(null);
                System.out.println("❌ Клиент не хотел это блюдо! Штраф -5$");
                return false;
            }
        } else {
            // Клиент уже обслужен или не хочет заказывать
            System.out.println("❌ Клиент не ожидает заказ");
            return false;
        }
    }

    // НОВЫЙ МЕТОД: ЗАПУСК ТАЙМЕРА ДЛЯ УХОДА КЛИЕНТА
    private void scheduleCustomerLeave(Customer customer, int delay) {
        // Останавливаем предыдущий таймер если есть
        if (customerLeaveTimers.containsKey(customer)) {
            customerLeaveTimers.get(customer).stop();
        }

        Timer leaveTimer = new Timer(delay, e -> {
            removeCustomer(customer);
            fireCustomerLeftHappy(customer); // Новое событие для довольного ухода
        });
        leaveTimer.setRepeats(false);
        leaveTimer.start();

        customerLeaveTimers.put(customer, leaveTimer);
        System.out.println("⏰ Клиент уйдет через " + (delay/1000) + " секунд");
    }

    // НОВЫЙ МЕТОД: УДАЛЕНИЕ КЛИЕНТА
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

    public void throwAwayDish() {
        if (player.getCarriedDish() != null) {
            player.setCarriedDish(null);
            score -= 3;
            fireDishThrownAway(new RestaurantEvent(this));
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

    // НОВОЕ СОБЫТИЕ: КЛИЕНТ УШЕЛ ДОВОЛЬНЫЙ
    private void fireCustomerLeftHappy(Customer customer) {
        for (RestaurantEventListener listener : listeners) {
            if (listener instanceof ExtendedRestaurantEventListener) {
                ((ExtendedRestaurantEventListener) listener).customerLeftHappy(new CustomerEvent(this, customer));
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
        for (RestaurantEventListener listener : listeners) {
            listener.orderTaken(new OrderEvent(this, new Order(null, dish)));
        }
    }

    private void fireOrderPlacedByWaiter(Dish dish) {
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
    public List<Order> getActiveOrders() { return activeOrders; }
    public List<Dish> getKitchenOrders() { return kitchenOrders; }
    public List<Dish> getKitchenQueue() { return kitchenQueue; }
    public List<Dish> getMenu() { return menu; }
}