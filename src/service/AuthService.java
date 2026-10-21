package service;

import database.DBConnection;
import model.Admin;
import model.Customer;
import model.User;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Locale;

public class AuthService {
    private static final int ITERATIONS = 210_000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    public Customer register(String name, String email, char[] password) throws SQLException {
        try {
            String cleanName = requireText(name, "Name", 100);
            String cleanEmail = requireText(email, "Email", 150).toLowerCase(Locale.ROOT);
            if (!cleanEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                throw new IllegalArgumentException("Enter a valid email address.");
            }
            validatePassword(password);

            String sql = "INSERT INTO users (full_name, email, password_hash, role) VALUES (?, ?, ?, 'CUSTOMER')";
            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, cleanName);
                statement.setString(2, cleanEmail);
                statement.setString(3, hashPassword(password));
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("User registration succeeded but no user ID was returned.");
                    }
                    return new Customer(keys.getLong(1), cleanName, cleanEmail);
                }
            }
        } finally {
            clear(password);
        }
    }

    public User login(String email, char[] password) throws SQLException {
        try {
            String cleanEmail = requireText(email, "Email", 150).toLowerCase(Locale.ROOT);
            validatePassword(password);
            String sql = "SELECT user_id, full_name, email, password_hash, role FROM users WHERE email = ?";
            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, cleanEmail);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next() || !verifyPassword(password, result.getString("password_hash"))) {
                        throw new IllegalArgumentException("Invalid email or password.");
                    }
                    long id = result.getLong("user_id");
                    String name = result.getString("full_name");
                    String storedEmail = result.getString("email");
                    String role = result.getString("role");
                    if ("ADMIN".equals(role)) {
                        return new Admin(id, name, storedEmail);
                    }
                    if ("CUSTOMER".equals(role)) {
                        return new Customer(id, name, storedEmail);
                    }
                    throw new SQLException("The account has an unsupported role.");
                }
            }
        } finally {
            clear(password);
        }
    }

    public static void requireAdmin(Admin admin) throws SQLException {
        requireRole(admin == null ? 0 : admin.getId(), "ADMIN");
    }

    public static void requireCustomer(Customer customer) throws SQLException {
        requireRole(customer == null ? 0 : customer.getId(), "CUSTOMER");
    }

    private static void requireRole(long userId, String requiredRole) throws SQLException {
        String sql = "SELECT role FROM users WHERE user_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next() || !requiredRole.equals(result.getString("role"))) {
                    throw new IllegalArgumentException(requiredRole + " account access is required.");
                }
            }
        }
    }

    private static String hashPassword(char[] password) {
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        byte[] hash = derive(password, salt, ITERATIONS);
        return ITERATIONS + ":" + Base64.getEncoder().encodeToString(salt) + ":"
                + Base64.getEncoder().encodeToString(hash);
    }

    private static boolean verifyPassword(char[] password, String storedHash) throws SQLException {
        String[] parts = storedHash.split(":", -1);
        if (parts.length != 3) {
            throw new SQLException("Stored password hash has an invalid format.");
        }
        try {
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expected = Base64.getDecoder().decode(parts[2]);
            if (iterations < 1 || salt.length == 0 || expected.length == 0) {
                throw new IllegalArgumentException("Invalid password hash parameters.");
            }
            return MessageDigest.isEqual(expected, derive(password, salt, iterations));
        } catch (IllegalArgumentException e) {
            throw new SQLException("Stored password hash has an invalid format.", e);
        }
    }

    private static byte[] derive(char[] password, byte[] salt, int iterations) {
        PBEKeySpec specification = new PBEKeySpec(password, salt, iterations, KEY_LENGTH);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(specification).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing is unavailable.", e);
        } finally {
            specification.clearPassword();
        }
    }

    private static void validatePassword(char[] password) {
        if (password == null || password.length < 8 || password.length > 128) {
            throw new IllegalArgumentException("Password must contain between 8 and 128 characters.");
        }
    }

    private static String requireText(String value, String label, int maxLength) {
        if (value == null || value.trim().isEmpty() || value.trim().length() > maxLength) {
            throw new IllegalArgumentException(label + " is required and must be at most " + maxLength + " characters.");
        }
        return value.trim();
    }

    private static void clear(char[] password) {
        if (password != null) {
            java.util.Arrays.fill(password, '\0');
        }
    }
}
