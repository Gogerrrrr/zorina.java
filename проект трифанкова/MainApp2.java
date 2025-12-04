import javax.swing.*;
import java.awt.*; // Добавляем этот импорт

public class MainApp2 {
    public static void main(String[] args) {
        // 1. Инициализируем базу данных
        Database.initDatabase();

        SwingUtilities.invokeLater(() -> {
            // Показываем приветственное окно
            showWelcomeDialog();

            // Запускаем игру
            RestaurantModel model = new RestaurantModel();
            RestaurantController controller = new RestaurantController(model);
            RestaurantView view = new RestaurantView(controller, model);
            controller.setView(view);
            view.setVisible(true);
        });
    }

    private static void showWelcomeDialog() {
        String rules = """
            🍕 ДОБРО ПОЖАЛОВАТЬ В РЕСТОРАННЫЙ СИМУЛЯТОР! 🍔
            
            Вы - Снежок, официант в ресторане. Ваша задача:
            
            🎯 ОСНОВНЫЕ ПРАВИЛА:
            1. 🏃 Двигайтесь стрелками
            2. 💬 Подходите к клиентам (ПРОБЕЛ) - узнайте их заказ
            3. 📝 Снова подойдите (ПРОБЕЛ) - примите заказ
            4. 🍳 Подойдите к кухне (K) - возьмите готовое блюдо
            5. 🏃 Отнесите клиенту (ПРОБЕЛ) - получите деньги
            6. 🧹 Убирайте грязные столы (ПРОБЕЛ)
            7. 🗑️ Выбрасывайте неправильные блюда (D)
            
            ⚠️ ВАЖНО:
            • Клиенты теряют терпение (зелёная→жёлтая→красная полоса)
            • Если клиент уйдёт - штраф 20$
            • Неправильный заказ - штраф 10$
            • Выброшенное блюдо - штраф 3 очка
            
            💰 ЗАРАБАТЫВАЙТЕ ДЕНЬГИ И СТАНОВИТЕСЬ ЛУЧШИМ ОФИЦИАНТОМ!
            """;

        JTextArea textArea = new JTextArea(rules);
        textArea.setEditable(false);
        textArea.setBackground(new Color(240, 248, 255)); // Теперь Color будет распознаваться
        textArea.setFont(new Font("Arial", Font.PLAIN, 14));
        textArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 500));

        JOptionPane.showMessageDialog(null, scrollPane,
                "🍽️ РЕСТОРАННЫЙ СИМУЛЯТОР - СНЕЖОК 🧊",
                JOptionPane.INFORMATION_MESSAGE);
    }
}