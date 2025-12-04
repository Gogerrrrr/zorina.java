import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GameRepository {

    public static void saveGameResult(String playerName, int score, int money) {
        String sql = "INSERT INTO game_results (player_name, score, money, date_time) VALUES (?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, playerName);
            pstmt.setInt(2, score);
            pstmt.setInt(3, money);
            pstmt.setString(4, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            pstmt.executeUpdate();
            System.out.println("✅ Результат игры сохранен в базе данных!");

        } catch (SQLException e) {
            System.out.println("❌ Ошибка при сохранении результата игры:");
            e.printStackTrace();
        }
    }

    // Метод сохранения заказа
    public static void saveOrder(String dishName, int price, Integer customerId, String status) {
        String sql = "INSERT INTO orders (dish_name, price, customer_id, status, order_time) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, dishName);
            pstmt.setInt(2, price);
            if (customerId == null) {
                pstmt.setNull(3, Types.INTEGER);
            } else {
                pstmt.setInt(3, customerId);
            }
            pstmt.setString(4, status);
            pstmt.setString(5, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            pstmt.executeUpdate();
            System.out.println("✅ Заказ сохранен в базе данных: " + dishName);

        } catch (SQLException e) {
            System.out.println("❌ Ошибка при сохранении заказа:");
            e.printStackTrace();
        }
    }

    public static void getAllGameResults() {
        String sql = "SELECT * FROM game_results ORDER BY score DESC LIMIT 10";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n🏆 ТОП-10 РЕЗУЛЬТАТОВ:");
            System.out.println("----------------------------------------");
            while (rs.next()) {
                System.out.printf("Игрок: %s | Счёт: %d | Деньги: $%d | Дата: %s%n",
                        rs.getString("player_name"),
                        rs.getInt("score"),
                        rs.getInt("money"),
                        rs.getString("date_time"));
            }
            System.out.println("----------------------------------------\n");

        } catch (SQLException e) {
            System.out.println("❌ Ошибка при получении результатов:");
            e.printStackTrace();
        }
    }
}