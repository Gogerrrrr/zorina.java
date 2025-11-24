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
        });
        gameTimer.start();
    }

    // ОСНОВНОЙ МЕТОД ЗАКАЗА
    public void placeOrderInKitchen(Dish dish) {
        System.out.println("🎯 КОНТРОЛЛЕР: Начало заказа " + dish.getName());

        Player player = model.getPlayer();
        if (isNearKitchen(player)) {
            System.out.println("📍 Игрок рядом с кухней");
            boolean success = model.placeOrderInKitchen(dish);

            if (success) {
                view.showMessage("🍳 Заказали: " + dish.getName() + " (-" + dish.getPrice() + "$, +5 очков)");
                view.repaint();
                System.out.println("✅ КОНТРОЛЛЕР: Заказ успешен");
            } else {
                if (model.getMoney() < dish.getPrice()) {
                    view.showMessage("❌ Недостаточно денег! Нужно: " + dish.getPrice() + "$, у вас: " + model.getMoney() + "$");
                } else {
                    view.showMessage("❌ Очередь кухни переполнена! Максимум 10 заказов");
                }
                System.out.println("❌ КОНТРОЛЛЕР: Заказ не удался");
            }
        } else {
            view.showMessage("Подойдите ближе к кухне!");
            System.out.println("❌ Игрок далеко от кухни");
        }
    }

    private void setupEventListeners() {
        ExtendedRestaurantEventListener extendedListener = new ExtendedRestaurantEventListener() {
            @Override
            public void customerAdded(CustomerEvent e) {
                view.showMessage("Новый клиент! Он хочет: " + e.getCustomer().getDesiredDish().getName());
            }

            @Override
            public void customerLeft(CustomerEvent e) {
                view.showMessage("❌ Клиент ушёл недовольный! Штраф -25$, -10 очков");
            }

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
                view.showMessage("✗ Ошибка! Клиент хотел " + e.getCustomer().getCurrentOrder().getDish().getName() + ", а вы принесли " + e.getWrongDish().getName() + ". Штраф -15$, -8 очков");
            }

            @Override
            public void dishThrownAway(RestaurantEvent e) {
                view.showMessage("🗑️ Блюдо выброшено! Штраф");
            }

            @Override
            public void gameWon(RestaurantEvent e) {
                gameTimer.stop();
                JOptionPane.showMessageDialog(view, "🎉 ПОЗДРАВЛЯЕМ! ВЫ ВЫИГРАЛИ!\nФинальный счёт: " + model.getScore() + " очков\nФинальный капитал: $" + model.getMoney(), "🏆 ПОБЕДА!", JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            }

            @Override
            public void gameOver(RestaurantEvent e) {
                gameTimer.stop();
                JOptionPane.showMessageDialog(view, "💀 ИГРА ОКОНЧЕНА! ВЫ ПРОИГРАЛИ!\nФинальный счёт: " + model.getScore() + " очков", "💥 ПРОИГРЫШ", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        };

        model.addRestaurantEventListener(extendedListener);
    }

    // Остальные методы без изменений...
    public void interactWithObject() {
        Player player = model.getPlayer();

        if (isNearTrashBin(player) && player.getCarriedDish() != null) {
            int choice = JOptionPane.showConfirmDialog(view, "Выбросить блюдо " + player.getCarriedDish().getName() + "?\nШтраф: -8$, -5 очков", "🗑️ Выбросить блюдо", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                model.throwAwayDishInTrashBin();
            }
            return;
        }

        for (Customer customer : model.getCustomers()) {
            if (isNear(player, customer)) {
                handleCustomerInteraction(customer);
                return;
            }
        }

        for (Table table : model.getTables()) {
            if (isNear(player, table) && !table.isClean()) {
                model.cleanupTable(table);
                view.showMessage("Стол убран! +5 очков");
                return;
            }
        }

        view.showMessage("Рядом нет объектов для взаимодействия");
    }

    private void handleCustomerInteraction(Customer customer) {
        Player player = model.getPlayer();

        if (player.getCarriedDish() != null) {
            boolean success = model.serveCustomer(customer);
            if (!success) {
                view.showMessage("Не удалось отдать заказ клиенту");
            }
            return;
        }

        if (customer.isServed()) {
            view.showMessage("Этот клиент уже обслужен и скоро уйдёт");
        } else if (customer.getCurrentOrder() != null) {
            view.showOrderInfo(customer);
        } else if (customer.getDesiredDish() != null) {
            int choice = JOptionPane.showConfirmDialog(view, "Клиент хочет заказать: " + customer.getDesiredDish().getName() + "\nПринять заказ?", "💬 Принять заказ", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                model.takeOrderFromCustomer(customer);
                view.showMessage("Заказ принят! Готовим: " + customer.getDesiredDish().getName());
            }
        }
    }

    private boolean isNearTrashBin(Player player) {
        int distance = (int) Math.sqrt(Math.pow(player.getX() - model.getTrashBinX(), 2) + Math.pow(player.getY() - model.getTrashBinY(), 2));
        return distance < 60;
    }

    public void takeFromKitchen() {
        Player player = model.getPlayer();
        if (isNearKitchen(player)) {
            if (model.getKitchenOrders().isEmpty()) {
                view.showMessage("На кухне нет готовых заказов! Заказов в очереди: " + model.getKitchenQueue().size());
            } else if (player.getCarriedDish() != null) {
                view.showMessage("У вас уже есть блюдо! Выбросьте его чтобы взять новое");
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
                view.showMessage("У вас уже есть блюдо! Выбросьте его чтобы взять новое");
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

    public void throwAwayDish() {
        if (model.getPlayer().getCarriedDish() != null) {
            int choice = JOptionPane.showConfirmDialog(view, "Выбросить блюдо " + model.getPlayer().getCarriedDish().getName() + "? (штраф -3 очка)", "Выбросить блюдо", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                model.throwAwayDish();
            }
        } else {
            view.showMessage("У вас нет блюда чтобы выбросить!");
        }
    }

    private boolean isNear(GameObject obj1, GameObject obj2) {
        int distance = (int) Math.sqrt(Math.pow(obj1.getX() - obj2.getX(), 2) + Math.pow(obj1.getY() - obj2.getY(), 2));
        return distance < 60;
    }

    private boolean isNearKitchen(Player player) {
        return player.getX() < 150 && player.getY() < 150;
    }

    public void addCustomer() {
        model.addCustomer();
        if (view != null) {
            view.requestGameFocus();
        }
    }

    public void showStats() {
        JOptionPane.showMessageDialog(view,
                "💰 Деньги: $" + model.getMoney() + "\n" +
                        "⭐ Счёт: " + model.getScore() + "/100\n" +
                        "👥 Клиентов: " + model.getCustomers().size() + "\n" +
                        "⏳ Заказов в очереди: " + model.getKitchenQueue().size() + "\n" +
                        "🍽️ Готовых заказов: " + model.getKitchenOrders().size());
    }
}