package server.db;

import java.sql.*;

public class DatabaseControler {

    private static final String URL = "jdbc:sqlite:" + System.getProperty("user.dir") + "/db/database.db";

    public static void initDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS requests (\n"
                + "	id      INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + "	street    CHARACTER(20) NOT NULL,\n"
                + "	num    CHARACTER(5) NOT NULL,\n"
                + "	phone    CHARACTER(15) NOT NULL\n"
                + ")";
        try (Connection conn = DriverManager.getConnection(DatabaseControler.URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public static int addRide(String street, String number, String phone) {
        String sql = "INSERT INTO requests (street, num, phone) OUTPUT Inserted.id VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DatabaseControler.URL);
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, street);
            statement.setString(2, number);
            statement.setString(3, phone);

            ResultSet rs = statement.executeQuery(sql.toString());
            while (rs.next()) {
                return rs.getInt(1);
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
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
