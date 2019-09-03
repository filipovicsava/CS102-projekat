package server.db;

import java.sql.*;

public class DatabaseControler {

    private static final String URL = "jdbc:sqlite:" + System.getProperty("user.dir") + "/db/database.db";

    public static void initDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS requests (\n"
                + "	id      INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + "	street    CHARACTER(20) NOT NULL UNIQUE ,\n"
                + "	number    CHARACTER(20) NOT NULL,\n"
                + ");";
        try (Connection conn = DriverManager.getConnection(DatabaseControler.URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public static void addRide(String address, String number) {
        String sql = "INSERT INTO requests (street, number) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DatabaseControler.URL);
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, address);
            statement.setString(2, number);


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void cancelRide(int id) {
        String sql = "DELETE FROM requests WHERE id = " + id;

        try (Connection conn = DriverManager.getConnection(DatabaseControler.URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
