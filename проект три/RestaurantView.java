import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.HashSet;
import java.util.Set;

public class RestaurantView extends JFrame {
    private RestaurantController controller;
    private RestaurantModel model;
    private GamePanel gamePanel;
    private JTextArea messageArea;
    private Set<Integer> pressedKeys;
    private Timer movementTimer;

    // Цвета для оформления
    private final Color WALL_COLOR = new Color(180, 160, 140);
    private final Color FLOOR_COLOR = new Color(240, 220, 180);
    private final Color TABLE_COLOR = new Color(139, 69, 19);
    private final Color DIRTY_TABLE_COLOR = new Color(100, 50, 20);
    private final Color KITCHEN_COLOR = new Color(80, 80, 80);

    public RestaurantView(RestaurantController controller, RestaurantModel model) {
        this.controller = controller;
        this.model = model;
        this.pressedKeys = new HashSet<>();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("🍕 Ресторанный симулятор - Снежок 🍔");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        gamePanel = new GamePanel();
        mainPanel.add(gamePanel, BorderLayout.CENTER);

        JPanel controlPanel = createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        setJMenuBar(createMenuBar());

        add(mainPanel);
        setupKeyListeners();

        movementTimer = new Timer(16, e -> {
            handleContinuousMovement();
        });
        movementTimer.start();
    }

    private void handleContinuousMovement() {
        int dx = 0, dy = 0;
        int speed = 4;

        if (pressedKeys.contains(KeyEvent.VK_UP)) dy -= speed;
        if (pressedKeys.contains(KeyEvent.VK_DOWN)) dy += speed;
        if (pressedKeys.contains(KeyEvent.VK_LEFT)) dx -= speed;
        if (pressedKeys.contains(KeyEvent.VK_RIGHT)) dx += speed;

        if (dx != 0 || dy != 0) {
            model.getPlayer().move(dx, dy);
            repaint();
        }
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 240, 240));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 240, 240));

        JButton addCustomerBtn = createStyledButton("👥 Новый клиент", new Color(100, 200, 100));
        JButton statsBtn = createStyledButton("📊 Статистика", new Color(100, 150, 255));
        JButton kitchenBtn = createStyledButton("🍳 Взять блюдо", new Color(255, 200, 100));
        JButton orderBtn = createStyledButton("📝 Заказать", new Color(200, 100, 255));
        JButton helpBtn = createStyledButton("❓ Помощь", new Color(255, 150, 100));

        JLabel scoreLabel = new JLabel("⭐ Счёт: 0");
        JLabel moneyLabel = new JLabel("💰 Деньги: $100");

        Font labelFont = new Font("Arial", Font.BOLD, 12);
        scoreLabel.setFont(labelFont);
        moneyLabel.setFont(labelFont);
        scoreLabel.setForeground(new Color(60, 60, 60));
        moneyLabel.setForeground(new Color(60, 60, 60));

        addCustomerBtn.addActionListener(e -> {
            controller.addCustomer();
            gamePanel.requestFocusInWindow();
        });
        statsBtn.addActionListener(e -> {
            controller.showStats();
            gamePanel.requestFocusInWindow();
        });
        kitchenBtn.addActionListener(e -> {
            controller.takeFromKitchen();
            gamePanel.requestFocusInWindow();
        });
        orderBtn.addActionListener(e -> {
            showOrderDialog();
            gamePanel.requestFocusInWindow();
        });
        helpBtn.addActionListener(e -> {
            showHelp();
            gamePanel.requestFocusInWindow();
        });

        buttonPanel.add(addCustomerBtn);
        buttonPanel.add(statsBtn);
        buttonPanel.add(kitchenBtn);
        buttonPanel.add(orderBtn);
        buttonPanel.add(helpBtn);
        buttonPanel.add(scoreLabel);
        buttonPanel.add(moneyLabel);

        messageArea = new JTextArea(3, 60);
        messageArea.setEditable(false);
        messageArea.setBackground(new Color(250, 250, 250));
        messageArea.setFont(new Font("Arial", Font.PLAIN, 12));
        messageArea.setForeground(Color.DARK_GRAY);
        messageArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JScrollPane messageScroll = new JScrollPane(messageArea);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(messageScroll, BorderLayout.CENTER);

        Timer uiTimer = new Timer(100, e -> {
            scoreLabel.setText("⭐ Счёт: " + model.getScore() + "/100");
            moneyLabel.setText("💰 Деньги: $" + model.getMoney());
        });
        uiTimer.start();

        JButton debugBtn = createStyledButton("🐛 Отладка", new Color(255, 100, 100));
        debugBtn.addActionListener(e -> {
            controller.debugKitchen();
            gamePanel.requestFocusInWindow();
        });
        buttonPanel.add(debugBtn);

        return panel;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return button;
    }

    private void showOrderDialog() {
        String[] options = {
                "🍽️ Сделать заказ на кухню",
                "📦 Взять конкретное блюдо",
                "📋 Посмотреть состояние кухни"
        };

        int choice = JOptionPane.showOptionDialog(this,
                "Управление кухней:",
                "🍳 Кухня",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]);

        switch (choice) {
            case 0:
                showPlaceOrderDialog();
                break;
            case 1:
                showTakeSpecificDishDialog();
                break;
            case 2:
                showKitchenStatus();
                break;
        }
    }

    private void showPlaceOrderDialog() {
        String[] options = model.getMenu().stream()
                .map(dish -> "🍽️ " + dish.getName() + " (+" + dish.getPrice() + "$)")
                .toArray(String[]::new);

        String choice = (String) JOptionPane.showInputDialog(
                this,
                "Какое блюдо заказать на кухне?",
                "📝 Заказ на кухне",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice != null) {
            String dishName = choice.substring(2).split(" \\+")[0];
            Dish selectedDish = model.getMenu().stream()
                    .filter(dish -> dish.getName().equals(dishName))
                    .findFirst()
                    .orElse(null);

            if (selectedDish != null) {
                controller.placeOrderInKitchen(selectedDish);
            }
        }
    }

    private void showTakeSpecificDishDialog() {
        if (model.getKitchenOrders().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "На кухне нет готовых блюд!\nЗаказов в очереди: " + model.getKitchenQueue().size(),
                    "🍳 Кухня", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] options = model.getKitchenOrders().stream()
                .map(dish -> "🍽️ " + dish.getName())
                .toArray(String[]::new);

        String choice = (String) JOptionPane.showInputDialog(
                this,
                "Какое блюдо взять?",
                "📦 Взять блюдо",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice != null) {
            String dishName = choice.substring(2);
            Dish selectedDish = model.getKitchenOrders().stream()
                    .filter(dish -> dish.getName().equals(dishName))
                    .findFirst()
                    .orElse(null);

            if (selectedDish != null) {
                controller.takeSpecificDishFromKitchen(selectedDish);
            }
        }
    }

    private void showKitchenStatus() {
        StringBuilder kitchenInfo = new StringBuilder();
        kitchenInfo.append("🍳 Состояние кухни:\n\n");

        kitchenInfo.append("Готовые заказы: ").append(model.getKitchenOrders().size()).append("\n");
        kitchenInfo.append("В очереди: ").append(model.getKitchenQueue().size()).append("\n\n");

        if (!model.getKitchenOrders().isEmpty()) {
            kitchenInfo.append("✅ Готовы к выдаче:\n");
            for (Dish dish : model.getKitchenOrders()) {
                kitchenInfo.append("• ").append(dish.getName()).append(" (+").append(dish.getPrice()).append("$)\n");
            }
            kitchenInfo.append("\n");
        }

        if (!model.getKitchenQueue().isEmpty()) {
            kitchenInfo.append("⏳ В процессе приготовления:\n");
            for (Dish dish : model.getKitchenQueue()) {
                int progress = model.getCookingProgress(dish);
                kitchenInfo.append("• ").append(dish.getName()).append(" - ").append(progress).append("%\n");
            }
        } else {
            kitchenInfo.append("📭 Очередь приготовления пуста\n");
        }


        JOptionPane.showMessageDialog(this, kitchenInfo.toString(), "🍳 Кухня", JOptionPane.INFORMATION_MESSAGE);
    }


    private void showHelp() {
        String helpText = """
            🎮 УПРАВЛЕНИЕ ИГРОЙ:
            
            ← ↑ ↓ →  - Движение Снежка
            ПРОБЕЛ   - Взаимодействие с клиентами и столами
            K        - Взять случайное блюдо с кухни
            D        - Выбросить блюдо
            H        - Помощь (это окно)
            O        - Быстрый заказ на кухне
            
            🎯 КАК ИГРАТЬ:
            
            1. Подойдите к клиенту (ПРОБЕЛ) - узнайте заказ
            2. Снова подойдите (ПРОБЕЛ) - примите заказ
            3. Используйте кнопку "📝 Заказать" для заказа блюд на кухне
            4. Подождите пока блюдо приготовится (следите за прогрессом)
            5. Используйте "🍳 Взять блюдо" или "📦 Взять конкретное блюдо"
            6. Отнесите клиенту (ПРОБЕЛ) - получите оплату
            7. Убирайте грязные столы (ПРОБЕЛ) - +5 очков
            
            🍳 СИСТЕМА ЗАКАЗОВ:
            • Заказывайте блюда заранее через меню "📝 Заказать"
            • Следите за прогрессом приготовления на кухне
            • Берите готовые блюда и относите клиентам
            
            ⚠️ ВНИМАНИЕ:
            • Следите за шкалой терпения клиентов
            • Клиенты уходят если терпение закончится (-20$)
            • Неправильный заказ = штраф 10$
            • Выброшенное блюдо = -3 очка
            
            💰 ЦЕЛЬ: Заработать как можно больше денег!
            """;

        JTextArea textArea = new JTextArea(helpText);
        textArea.setEditable(false);
        textArea.setBackground(new Color(240, 248, 255));
        textArea.setFont(new Font("Arial", Font.PLAIN, 14));
        textArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));

        JOptionPane.showMessageDialog(this, scrollPane,
                "❓ Помощь по игре", JOptionPane.INFORMATION_MESSAGE);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(70, 130, 180));

        JMenu gameMenu = new JMenu("🎮 Игра");
        gameMenu.setForeground(Color.WHITE);
        gameMenu.setFont(new Font("Arial", Font.BOLD, 12));

        JMenuItem newGameItem = new JMenuItem("🆕 Новая игра");
        JMenuItem kitchenItem = new JMenuItem("🍳 Управление кухней");
        JMenuItem helpItem = new JMenuItem("❓ Помощь");
        JMenuItem exitItem = new JMenuItem("🚪 Выход");

        newGameItem.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Начать новую игру?", "Новая игра", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(this, "Новая игра началась!");
                // Здесь можно добавить сброс состояния игры
            }
            gamePanel.requestFocusInWindow();
        });

        kitchenItem.addActionListener(e -> {
            showOrderDialog();
            gamePanel.requestFocusInWindow();
        });

        helpItem.addActionListener(e -> {
            showHelp();
            gamePanel.requestFocusInWindow();
        });

        exitItem.addActionListener(e -> System.exit(0));

        gameMenu.add(newGameItem);
        gameMenu.add(kitchenItem);
        gameMenu.add(helpItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);

        menuBar.add(gameMenu);
        return menuBar;
    }

    private void setupKeyListeners() {
        gamePanel.setFocusable(true);
        gamePanel.requestFocusInWindow();

        gamePanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                pressedKeys.add(e.getKeyCode());

                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    controller.interactWithObject();
                } else if (e.getKeyCode() == KeyEvent.VK_K) {
                    controller.takeFromKitchen();
                } else if (e.getKeyCode() == KeyEvent.VK_D) {
                    controller.throwAwayDish();
                } else if (e.getKeyCode() == KeyEvent.VK_H) {
                    showHelp();
                } else if (e.getKeyCode() == KeyEvent.VK_O) {
                    showOrderDialog(); // Быстрый доступ к заказу
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                pressedKeys.remove(e.getKeyCode());
            }
        });

        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                gamePanel.requestFocusInWindow();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                gamePanel.requestFocusInWindow();
            }
        });
    }

    public void repaint() {
        if (gamePanel != null) {
            gamePanel.repaint();
        }
    }

    public void showMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            messageArea.append("• " + msg + "\n");
            messageArea.setCaretPosition(messageArea.getDocument().getLength());
        });
    }

    public void showCustomerDesire(Customer customer) {
        JOptionPane.showMessageDialog(this,
                "👤 Клиент говорит:\n\n" +
                        "\"Я бы хотел заказать " + customer.getDesiredDish().getName() +
                        "!\"\n\n" +
                        "Подойдите ещё раз чтобы принять заказ",
                "💬 Заказ клиента",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void showOrderInfo(Customer customer) {
        String status = "";
        boolean inQueue = false;

        // Проверяем, где находится заказ клиента
        for (Dish dish : model.getKitchenQueue()) {
            if (dish.getName().equals(customer.getCurrentOrder().getDish().getName())) {
                inQueue = true;
                int progress = model.getCookingProgress(dish);
                status = "⏳ готовится на кухне (" + progress + "%)";
                break;
            }
        }

        if (status.isEmpty()) {
            for (Dish dish : model.getKitchenOrders()) {
                if (dish.getName().equals(customer.getCurrentOrder().getDish().getName())) {
                    status = "✅ готов к выдаче";
                    break;
                }
            }
        }

        if (status.isEmpty()) {
            status = "📝 принят, ожидает приготовления";
        }

        String message = "📋 Информация о заказе:\n\n" +
                "🍽️ Блюдо: " + customer.getCurrentOrder().getDish().getName() +
                "\n💰 Цена: +" + customer.getCurrentOrder().getDish().getPrice() + "$\n" +
                "📊 Статус: " + status + "\n\n";

        if (inQueue) {
            message += "Подождите пока блюдо приготовится,\nзатем возьмите его с кухни и принесите клиенту";
        } else if (status.equals("✅ готов к выдаче")) {
            message += "Блюдо готово! Возьмите его с кухни\nи принесите клиенту";
        }

        JOptionPane.showMessageDialog(this, message,
                "📋 Информация о заказе",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void requestGameFocus() {
        if (gamePanel != null) {
            gamePanel.requestFocusInWindow();
        }
    }

    // ВНУТРЕННИЙ КЛАСС GamePanel
    private class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            drawRestaurant(g2d);
            drawKitchen(g2d);

            for (Table table : model.getTables()) {
                drawTable(g2d, table);
            }

            for (Customer customer : model.getCustomers()) {
                drawCustomer(g2d, customer);
            }

            drawPlayer(g2d, model.getPlayer());
            drawKitchenOrders(g2d);
            drawUI(g2d);
        }

        private void drawRestaurant(Graphics2D g2d) {
            // Фон с градиентом
            GradientPaint background = new GradientPaint(0, 0, new Color(230, 240, 255),
                    getWidth(), getHeight(), new Color(210, 230, 255));
            g2d.setPaint(background);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            // Стены
            g2d.setColor(WALL_COLOR);
            g2d.fillRect(0, 0, getWidth(), 80);
            g2d.fillRect(0, 0, 80, getHeight());
            g2d.fillRect(getWidth() - 80, 0, 80, getHeight());
            g2d.fillRect(0, getHeight() - 60, getWidth(), 60);

            // Пол с узором
            g2d.setColor(FLOOR_COLOR);
            g2d.fillRect(80, 80, getWidth() - 160, getHeight() - 140);

            // Узор на полу
            g2d.setColor(new Color(220, 200, 170));
            for (int x = 80; x < getWidth() - 80; x += 40) {
                for (int y = 80; y < getHeight() - 60; y += 40) {
                    g2d.drawRect(x, y, 40, 40);
                }
            }

            // Название ресторана на стене
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            String title = "🍕 РЕСТОРАН СНЕЖОКА 🍔";
            int titleWidth = g2d.getFontMetrics().stringWidth(title);
            g2d.drawString(title, (getWidth() - titleWidth) / 2, 50);
        }

        private void drawKitchen(Graphics2D g2d) {
            // Основание кухни
            g2d.setColor(KITCHEN_COLOR);
            g2d.fillRoundRect(40, 100, 120, 80, 20, 20);

            // Столешница
            g2d.setColor(new Color(100, 100, 100));
            g2d.fillRect(35, 95, 130, 10);

            // Плита
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(60, 120, 40, 30);
            g2d.setColor(Color.RED);
            g2d.fillOval(70, 130, 8, 8);
            g2d.fillOval(85, 130, 8, 8);

            // Раковина
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(110, 120, 30, 25);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString("🍳 КУХНЯ", 60, 95);

            // Информация о кухне
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            g2d.drawString("Готово: " + model.getKitchenOrders().size(), 45, 110);
            g2d.drawString("В очереди: " + model.getKitchenQueue().size(), 45, 125);

            // Отображение прогресса приготовления
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));

            int yOffset = 140;
            for (Dish dish : model.getKitchenQueue()) {
                int progress = model.getCookingProgress(dish);
                g2d.drawString(dish.getName() + ": " + progress + "%", 45, yOffset);
                yOffset += 12;

                // Прогресс-бар
                g2d.setColor(new Color(200, 200, 200));
                g2d.fillRect(45, yOffset, 60, 6);
                g2d.setColor(new Color(0, 150, 0));
                g2d.fillRect(45, yOffset, (progress * 60) / 100, 6);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(45, yOffset, 60, 6);

                yOffset += 15;

                // Ограничиваем отображение
                if (yOffset > 250) break;
            }
        }

        private void drawTable(Graphics2D g2d, Table table) {
            int x = table.getX();
            int y = table.getY();

            // Ножки стола
            g2d.setColor(new Color(101, 67, 33));
            g2d.fillRect(x - 20, y + 15, 8, 20);
            g2d.fillRect(x + 12, y + 15, 8, 20);

            // Столешница
            if (table.isClean()) {
                g2d.setColor(TABLE_COLOR);
            } else {
                g2d.setColor(DIRTY_TABLE_COLOR);
            }
            g2d.fillRoundRect(x - 30, y - 25, 60, 50, 15, 15);

            // Текстура стола
            g2d.setColor(table.isClean() ? new Color(160, 120, 80) : new Color(120, 80, 40));
            g2d.drawRoundRect(x - 30, y - 25, 60, 50, 15, 15);

            // Номер стола
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            String tableNum = String.valueOf(table.getTableNumber() + 1);
            int textWidth = g2d.getFontMetrics().stringWidth(tableNum);
            g2d.drawString(tableNum, x - textWidth/2, y + 5);

            // Если стол грязный - добавляем визуальный индикатор
            if (!table.isClean()) {
                g2d.setColor(new Color(100, 100, 100, 150));
                g2d.fillOval(x - 10, y - 10, 20, 20);
                g2d.setColor(Color.WHITE);
                g2d.drawString("💩", x - 8, y + 5);
            }
        }

        private void drawCustomer(Graphics2D g2d, Customer customer) {
            int x = customer.getX();
            int y = customer.getY();

            // Тень
            g2d.setColor(new Color(0, 0, 0, 50));
            g2d.fillOval(x - 18, y + 10, 36, 8);

            // Тело (одежда)
            g2d.setColor(customer.getColor());
            g2d.fillRoundRect(x - 20, y - 15, 40, 30, 10, 10);

            // Голова
            g2d.setColor(new Color(255, 218, 185));
            g2d.fillOval(x - 15, y - 35, 30, 30);

            // Волосы
            g2d.setColor(new Color(80, 50, 20));
            g2d.fillRect(x - 15, y - 35, 30, 8);

            // Глаза
            g2d.setColor(Color.WHITE);
            g2d.fillOval(x - 8, y - 25, 6, 6);
            g2d.fillOval(x + 2, y - 25, 6, 6);
            g2d.setColor(Color.BLACK);
            g2d.fillOval(x - 6, y - 23, 3, 3);
            g2d.fillOval(x + 4, y - 23, 3, 3);

            // Рот в зависимости от настроения
            if (customer.isServed()) {
                g2d.setColor(Color.RED);
                g2d.drawArc(x - 5, y - 18, 10, 6, 0, -180); // Улыбка
            } else if (customer.getPatience() > customer.getMaxPatience() * 0.3) {
                g2d.setColor(Color.BLACK);
                g2d.drawLine(x - 4, y - 17, x + 4, y - 17); // Нейтральный
            } else {
                g2d.setColor(Color.RED);
                g2d.drawArc(x - 5, y - 15, 10, 6, 0, 180); // Грустный
            }

            // Шкала терпения (увеличенная)
            int patience = customer.getPatience();
            int maxPatience = customer.getMaxPatience();
            int barWidth = 60;
            int filledWidth = (patience * barWidth) / maxPatience;

            // Фон шкалы
            g2d.setColor(new Color(200, 200, 200));
            g2d.fillRect(x - 30, y - 55, barWidth, 8);

            // Заполнение шкалы
            if (patience > maxPatience * 0.6) {
                g2d.setColor(Color.GREEN);
            } else if (patience > maxPatience * 0.3) {
                g2d.setColor(Color.YELLOW);
            } else {
                g2d.setColor(Color.RED);
            }
            g2d.fillRect(x - 30, y - 55, filledWidth, 8);

            // Контур шкалы
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x - 30, y - 55, barWidth, 8);

            // Индикаторы состояния
            if (customer.getDesiredDish() != null && customer.getCurrentOrder() == null) {
                // Хочет заказать
                drawSpeechBubble(g2d, x + 25, y - 45, "❓");
            } else if (customer.getCurrentOrder() != null && !customer.isServed()) {
                // Ждет заказ
                drawSpeechBubble(g2d, x + 25, y - 45, "⏰");
            }
        }

        private void drawSpeechBubble(Graphics2D g2d, int x, int y, String symbol) {
            g2d.setColor(new Color(255, 255, 255, 200));
            g2d.fillOval(x, y, 20, 20);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(x, y, 20, 20);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString(symbol, x + 5, y + 15);
        }

        private void drawPlayer(Graphics2D g2d, Player player) {
            int x = player.getX();
            int y = player.getY();

            // Тень
            g2d.setColor(new Color(0, 0, 0, 50));
            g2d.fillOval(x - 22, y + 12, 44, 10);

            // Тело Снежка (белый круг)
            g2d.setColor(Color.WHITE);
            g2d.fillOval(x - 25, y - 25, 50, 50);

            // Детали Снежка
            g2d.setColor(new Color(200, 200, 200));
            g2d.drawOval(x - 25, y - 25, 50, 50);

            // Глаза
            g2d.setColor(Color.BLACK);
            g2d.fillOval(x - 10, y - 10, 8, 8);
            g2d.fillOval(x + 2, y - 10, 8, 8);

            // Блеск в глазах
            g2d.setColor(Color.WHITE);
            g2d.fillOval(x - 8, y - 8, 3, 3);
            g2d.fillOval(x + 4, y - 8, 3, 3);

            // Улыбка
            g2d.setColor(Color.BLACK);
            g2d.drawArc(x - 8, y - 3, 16, 10, 0, -180);

            // Нос-морковка
            g2d.setColor(Color.ORANGE);
            Polygon nose = new Polygon();
            nose.addPoint(x, y - 5);
            nose.addPoint(x - 4, y);
            nose.addPoint(x + 4, y);
            g2d.fillPolygon(nose);

            // Если несет блюдо
            if (player.getCarriedDish() != null) {
                drawDish(g2d, x + 20, y - 30, player.getCarriedDish());
            }
        }

        private void drawDish(Graphics2D g2d, int x, int y, Dish dish) {
            // Тарелка
            g2d.setColor(Color.WHITE);
            g2d.fillOval(x, y, 25, 25);
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawOval(x, y, 25, 25);

            // Еда
            g2d.setColor(dish.getColor());
            g2d.fillOval(x + 5, y + 5, 15, 15);

            // Пар от горячего блюда
            if (dish.getName().equals("Суп") || dish.getName().equals("Паста")) {
                g2d.setColor(new Color(200, 200, 255, 150));
                for (int i = 0; i < 3; i++) {
                    g2d.fillOval(x + 28, y + 5 + i * 6, 4, 4);
                }
            }

            // Название блюда
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 9));
            String name = dish.getName().length() > 4 ? dish.getName().substring(0, 4) : dish.getName();
            g2d.drawString(name, x + 5, y + 35);
        }

        private void drawKitchenOrders(Graphics2D g2d) {
            int startX = 50;
            int startY = 190;

            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString("Готовые заказы:", 45, 185);

            for (int i = 0; i < model.getKitchenOrders().size() && i < 3; i++) {
                Dish dish = model.getKitchenOrders().get(i);
                drawDish(g2d, startX, startY + i * 35, dish);
            }
        }

        private void drawUI(Graphics2D g2d) {
            // Подсказки управления
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            g2d.drawString("Управление: ←↑↓→ двигаться, ПРОБЕЛ взаимодействовать, K кухня, D выбросить, O заказ, H помощь", 10, getHeight() - 25);

            // Информация о несомом блюде
            if (model.getPlayer().getCarriedDish() != null) {
                g2d.setColor(Color.RED);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                g2d.drawString("Несёте: " + model.getPlayer().getCarriedDish().getName(), 10, getHeight() - 10);
            }

            // Предупреждение о фокусе
            if (!hasFocus()) {
                g2d.setColor(new Color(255, 50, 50, 200));
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                String message = "🔴 КЛИКНИТЕ СЮДА ДЛЯ УПРАВЛЕНИЯ";
                int textWidth = g2d.getFontMetrics().stringWidth(message);
                g2d.drawString(message, (getWidth() - textWidth) / 2, 40);
            }
        }
    }
}