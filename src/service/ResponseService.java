package service;

import database.DBConnection;
import model.Admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ResponseService {
    public void respond(Admin admin, long feedbackId, String responseText) throws SQLException {
        AuthService.requireAdmin(admin);
        if (feedbackId <= 0) {
            throw new IllegalArgumentException("Feedback ID must be positive.");
        }
        if (responseText == null || responseText.trim().isEmpty() || responseText.trim().length() > 500) {
            throw new IllegalArgumentException("Response is required and must be at most 500 characters.");
        }
        String sql = "INSERT INTO responses (feedback_id, admin_id, response_text) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE admin_id = VALUES(admin_id), response_text = VALUES(response_text), "
                + "created_at = CURRENT_TIMESTAMP";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, feedbackId);
            statement.setLong(2, admin.getId());
            statement.setString(3, responseText.trim());
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Administrator response was not saved.");
            }
        }
    }
}
