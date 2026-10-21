package database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    public static Connection getConnection() throws SQLException {
        Properties props = new Properties();

        try (FileInputStream in = new FileInputStream("db.properties")) {
            props.load(in);
        } catch (IOException e) {
            throw new SQLException("Could not read db.properties. Copy db.properties.example and configure it.", e);
        }

        String url = setting("DB_URL", "db.url", props);
        String user = setting("DB_USER", "db.user", props);
        String password = setting("DB_PASSWORD", "db.password", props);

        if (url == null || user == null || password == null) {
            throw new SQLException("db.url, db.user, and db.password must be configured.");
        }

        return DriverManager.getConnection(url, user, password);
    }

    private static String setting(String environmentName, String propertyName, Properties props) {
        String environmentValue = System.getenv(environmentName);
        return environmentValue == null || environmentValue.trim().isEmpty()
                ? props.getProperty(propertyName)
                : environmentValue;
    }
}