import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:db/restaurant.db";


    public static Connection getConnection() throws SQLException {

        return DriverManager.getConnection(DB_URL);
    }


    public static void initDatabase() {

        String createGameResultsTable = """
            CREATE TABLE IF NOT EXISTS game_results (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                player_name TEXT NOT NULL,
                score INTEGER NOT NULL,
                money INTEGER NOT NULL,
                date_time TEXT NOT NULL
            );
            """;
        String createOrdersTable = """
            CREATE TABLE IF NOT EXISTS orders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                dish_name TEXT NOT NULL,
                price INTEGER NOT NULL,
                customer_id INTEGER,
                status TEXT NOT NULL,
                order_time TEXT NOT NULL
            );
            """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createGameResultsTable);
            stmt.execute(createOrdersTable);

            System.out.println("✅ База данных и таблицы успешно инициализированы.");

        } catch (SQLException e) {
            System.out.println("❌ Ошибка при инициализации базы данных:");
            e.printStackTrace();
        }
    }
}