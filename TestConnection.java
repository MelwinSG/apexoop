import database.DBConnection;
import java.sql.Connection;

public class TestConnection {
public static void main(String[] args) {
try (Connection connection = DBConnection.getConnection()) {
System.out.println("SUCCESS: Connected to TiDB Cloud!");
System.out.println("Database: " + connection.getCatalog());
System.out.println("Connection is valid: " + connection.isValid(5));
} catch (Exception e) {
System.out.println("Connection failed!");
e.printStackTrace();
}
}
}
