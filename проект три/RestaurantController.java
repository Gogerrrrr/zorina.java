import java.awt.event.*;
import javax.swing.*;

public class RestaurantController {
    private RestaurantModel model;
    private RestaurantView view;
    private Timer gameTimer;

    public RestaurantController(RestaurantModel model) {
        this.model = model;
        setupTimer();
        setupEventListeners();
    }

    public void setView(RestaurantView view) {
        this.view = view;
    }

    private void setupTimer() {
        gameTimer = new Timer(50, e -> {
            model.updateCustomers();
            view.repaint();
            checkGameStatus();
        });
        gameTimer.start();
    }

    private void setupEventListeners() {
        ExtendedRestaurantEventListener extendedListener = new ExtendedRestaurantEventListener() {
            @Override
            public void customerAdded(CustomerEvent e) {
                view.showMessage("Новый клиент! Он хочет заказать: " +
                        e.getCustomer().getDesiredDish().getName());
            }

            @Override
            public void customerLeft(CustomerEvent e) {
                view.showMessage("❌ Клиент ушёл недовольный! Штраф -20$");
            }

            // НОВОЕ СОБЫТИЕ: КЛИЕНТ УШЕЛ ДОВОЛЬНЫЙ
            @Override
            public void customerLeftHappy(CustomerEvent e) {
                view.showMessage("✅ Клиент ушёл довольный! Место освободилось");
            }

            @Override
            public void orderTaken(OrderEvent e) {
                if (e.getOrder().getCustomer() != null) {
                    view.showMessage("Заказ принят! Готовим: " + e.getOrder().getDish().getName());
                } else {
                    view.showMessage("✅ Блюдо готово: " + e.getOrder().getDish().getName());
                }
            }

            @Override
            public void orderServed(OrderEvent e) {
                view.showMessage("✓ Отличная работа! +" + e.getOrder().getDish().getPrice() + "$ и +15 очков");
            }

            @Override
            public void wrongOrderServed(WrongOrderEvent e) {
                view.showMessage("✗ Ошибка! Клиент хотел " +
                        e.getCustomer().getCurrentOrder().getDish().getName() +
                        ", а вы принесли " + e.getWrongDish().getName() + ". Штраф -10$");
            }

            @Override
            public void dishThrownAway(RestaurantEvent e) {
                view.showMessage("Блюдо выброшено! Штраф -3 очка");
            }

            @Override
            public void gameWon(RestaurantEvent e) {
                gameTimer.stop();
                JOptionPane.showMessageDialog(view,
                        "🎉 ПОЗДРАВЛЯЕМ! ВЫ ВЫИГРАЛИ! 🎉\n\n" +
                                "Вы достигли 100 очков и стали лучшим официантом!\n" +
                                "Финальный счёт: " + model.getScore() + " очков\n" +
                                "Финальный капитал: $" + model.getMoney() + "\n" +
                                "Обслужено клиентов: " + countServedCustomers(),
                        "🏆 ПОБЕДА!",
                        JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            }
        };

        model.addRestaurantEventListener(extendedListener);
    }

    // ОСНОВНОЙ МЕТОД ВЗАИМОДЕЙСТВИЯ
    public void interactWithObject() {
        Player player = model.getPlayer();

        // Взаимодействие с клиентами
        for (Customer customer : model.getCustomers()) {
            if (isNear(player, customer)) {
                handleCustomerInteraction(customer);
                return;
            }
        }

        // Взаимодействие со столами
        for (Table table : model.getTables()) {
            if (isNear(player, table) && !table.isClean()) {
                model.cleanupTable(table);
                view.showMessage("Стол убран! +5 очков");
                return;
            }
        }

        view.showMessage("Рядом нет объектов для взаимодействия");
    }

    // МЕТОД ДЛЯ ОБРАБОТКИ ВЗАИМОДЕЙСТВИЯ С КЛИЕНТАМИ
    private void handleCustomerInteraction(Customer customer) {
        Player player = model.getPlayer();

        if (player.getCarriedDish() != null) {
            // Официант несет блюдо - пробуем отдать клиенту
            boolean success = model.serveCustomer(customer);
            if (!success) {
                view.showMessage("Не удалось отдать заказ клиенту");
            }
            return;
        }

        // Официант пустой - взаимодействуем с клиентом
        if (customer.isServed()) {
            view.showMessage("Этот клиент уже обслужен и скоро уйдёт");
        } else if (customer.getCurrentOrder() != null) {
            // У клиента есть активный заказ
            view.showOrderInfo(customer);
        } else if (customer.getDesiredDish() != null) {
            // Клиент хочет сделать заказ
            int choice = JOptionPane.showConfirmDialog(view,
                    "Клиент хочет заказать: " + customer.getDesiredDish().getName() +
                            "\nПринять заказ?",
                    "💬 Принять заказ",
                    JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                model.takeOrderFromCustomer(customer);
                view.showMessage("Заказ принят! Готовим: " + customer.getDesiredDish().getName());
            }
        }
    }

    // Остальные методы остаются без изменений...
    public void takeFromKitchen() {
        Player player = model.getPlayer();
        if (isNearKitchen(player)) {
            if (model.getKitchenOrders().isEmpty()) {
                view.showMessage("На кухне нет готовых заказов! Заказов в очереди: " +
                        model.getKitchenQueue().size());
            } else if (player.getCarriedDish() != null) {
                view.showMessage("У вас уже есть блюдо! Выбросьте его (D) чтобы взять новое");
            } else {
                model.takeOrderFromKitchen();
                Dish dish = player.getCarriedDish();
                if (dish != null) {
                    view.showMessage("Взяли с кухни: " + dish.getName());
                }
            }
        } else {
            view.showMessage("Подойдите ближе к кухне!");
        }
    }

    public void takeSpecificDishFromKitchen(Dish dish) {
        Player player = model.getPlayer();
        if (isNearKitchen(player)) {
            if (player.getCarriedDish() != null) {
                view.showMessage("У вас уже есть блюдо! Выбросьте его (D) чтобы взять новое");
            } else {
                boolean success = model.takeSpecificDishFromKitchen(dish);
                if (success) {
                    view.showMessage("Взяли с кухни: " + dish.getName());
                } else {
                    view.showMessage("Блюдо " + dish.getName() + " ещё не готово!");
                }
            }
        } else {
            view.showMessage("Подойдите ближе к кухне!");
        }
    }

    public void placeOrderInKitchen(Dish dish) {
        Player player = model.getPlayer();
        if (isNearKitchen(player)) {
            boolean success = model.placeOrderInKitchen(dish);
            if (success) {
                view.showMessage("Заказали на кухне: " + dish.getName() +
                        " (-" + dish.getPrice() + "$, +5 очков)");
            } else {
                if (model.getMoney() < dish.getPrice()) {
                    view.showMessage("Недостаточно денег для заказа " + dish.getName() +
                            "! Нужно: " + dish.getPrice() + "$, у вас: " + model.getMoney() + "$");
                } else {
                    view.showMessage("Ошибка при заказе!");
                }
            }
        } else {
            view.showMessage("Подойдите ближе к кухне!");
        }
    }

    public void throwAwayDish() {
        if (model.getPlayer().getCarriedDish() != null) {
            int choice = JOptionPane.showConfirmDialog(view,
                    "Выбросить блюдо " + model.getPlayer().getCarriedDish().getName() + "? (штраф -3 очка)",
                    "Выбросить блюдо",
                    JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                model.throwAwayDish();
            }
        } else {
            view.showMessage("У вас нет блюда чтобы выбросить!");
        }
    }

    private boolean isNear(GameObject obj1, GameObject obj2) {
        int distance = (int) Math.sqrt(
                Math.pow(obj1.getX() - obj2.getX(), 2) +
                        Math.pow(obj1.getY() - obj2.getY(), 2)
        );
        return distance < 60;
    }

    private boolean isNearKitchen(Player player) {
        return player.getX() < 150 && player.getY() < 150;
    }

    private void checkGameStatus() {
        if (model.getMoney() <= 0) {
            gameTimer.stop();
            JOptionPane.showMessageDialog(view,
                    "Игра окончена! Вы обанкротились!\n" +
                            "Ваш счёт: " + model.getScore() + "\n" +
                            "Обслужено клиентов: " + countServedCustomers());
            System.exit(0);
        }
    }

    private int countServedCustomers() {
        int count = 0;
        for (Customer customer : model.getCustomers()) {
            if (customer.isServed()) count++;
        }
        return count;
    }

    public void addCustomer() {
        model.addCustomer();
        if (view != null) {
            view.requestGameFocus();
        }
    }
    // ДОБАВЬТЕ ЭТОТ МЕТОД В КОНЕЦ КЛАССА RestaurantController:
    public void debugKitchen() {
        System.out.println("=== ОТЛАДКА КУХНИ ===");
        System.out.println("Меню: " + model.getMenu().size() + " блюд");
        for (Dish dish : model.getMenu()) {
            System.out.println("  - " + dish.getName() + " (" + dish.getPrice() + "$)");
        }
        System.out.println("Очередь приготовления: " + model.getKitchenQueue().size() + " блюд");
        for (Dish dish : model.getKitchenQueue()) {
            int progress = model.getCookingProgress(dish);
            System.out.println("  - " + dish.getName() + " - " + progress + "%");
        }
        System.out.println("Готовые заказы: " + model.getKitchenOrders().size() + " блюд");
        for (Dish dish : model.getKitchenOrders()) {
            System.out.println("  - " + dish.getName());
        }
        System.out.println("Клиенты: " + model.getCustomers().size());
        for (Customer customer : model.getCustomers()) {
            String status = customer.isServed() ? "обслужен" :
                    customer.getCurrentOrder() != null ? "ждет заказ" :
                            "хочет заказать";
            System.out.println("  - " + status + ": " +
                    (customer.getDesiredDish() != null ? customer.getDesiredDish().getName() : "нет"));
        }
        System.out.println("Деньги: $" + model.getMoney());
        System.out.println("Очки: " + model.getScore() + "/100");
        System.out.println("====================");
    }

    public void showStats() {
        JOptionPane.showMessageDialog(view,
                "💰 Деньги: $" + model.getMoney() + "\n" +
                        "⭐ Счёт: " + model.getScore() + "/100\n" +
                        "👥 Клиентов: " + model.getCustomers().size() + "\n" +
                        "✅ Обслужено: " + countServedCustomers() + "\n" +
                        "⏳ Заказов в очереди: " + model.getKitchenQueue().size() + "\n" +
                        "🍽️ Готовых заказов: " + model.getKitchenOrders().size());
    }
}