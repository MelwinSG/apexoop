package service;

import database.DBConnection;
import model.Admin;
import model.Customer;
import model.Feedback;
import model.Response;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FeedbackManager {
    public void submit(Customer customer, long productId, int rating, String comment) throws SQLException {
        AuthService.requireCustomer(customer);
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be from 1 to 5.");
        }
        String cleanComment = requireText(comment, "Comment", 500);
        String sql = "INSERT INTO feedback (user_id, product_id, rating, comment_text) VALUES (?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, customer.getId());
            statement.setLong(2, productId);
            statement.setInt(3, rating);
            statement.setString(4, cleanComment);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Feedback was not saved.");
            }
        }
    }

    public List<Feedback> listCustomerFeedback(Customer customer, String search, Long productId, Integer rating)
            throws SQLException {
        return listCustomerFeedback(customer, search, productId, rating, null, null);
    }

    public List<Feedback> listCustomerFeedback(Customer customer, String search, Long productId, Integer rating,
                                                LocalDate fromDate, LocalDate toDate) throws SQLException {
        return listCustomerFeedback(customer, search, productId, rating, null, fromDate, toDate);
    }

    public List<Feedback> listCustomerFeedback(Customer customer, String search, Long productId, Integer rating,
                                                String sentiment, LocalDate fromDate, LocalDate toDate)
            throws SQLException {
        AuthService.requireCustomer(customer);
        return queryFeedback(customer.getId(), search, productId, rating, sentiment, fromDate, toDate, null);
    }

    public List<Feedback> listRecentCustomerFeedback(Customer customer, int limit) throws SQLException {
        AuthService.requireCustomer(customer);
        return queryFeedback(customer.getId(), "", null, null, null, null, null, requireLimit(limit));
    }

    public List<Feedback> listAllFeedback(Admin admin, String search, Long productId, Integer rating)
            throws SQLException {
        return listAllFeedback(admin, search, productId, rating, null, null);
    }

    public List<Feedback> listAllFeedback(Admin admin, String search, Long productId, Integer rating,
                                          LocalDate fromDate, LocalDate toDate) throws SQLException {
        return listAllFeedback(admin, search, productId, rating, null, fromDate, toDate);
    }

    public List<Feedback> listAllFeedback(Admin admin, String search, Long productId, Integer rating,
                                          String sentiment, LocalDate fromDate, LocalDate toDate)
            throws SQLException {
        AuthService.requireAdmin(admin);
        return queryFeedback(null, search, productId, rating, sentiment, fromDate, toDate, null);
    }

    public List<Feedback> listRecentFeedback(Admin admin, int limit) throws SQLException {
        AuthService.requireAdmin(admin);
        return queryFeedback(null, "", null, null, null, null, null, requireLimit(limit));
    }

    public List<Feedback> listCommunityFeedback(User user) throws SQLException {
        AuthService.requireRegisteredUser(user);
        return queryFeedback(null, "", null, null, null, null, null, null);
    }

    public void deleteFeedback(Admin admin, long feedbackId) throws SQLException {
        AuthService.requireAdmin(admin);
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM responses WHERE feedback_id = ?")) {
                    statement.setLong(1, feedbackId);
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM feedback WHERE feedback_id = ?")) {
                    statement.setLong(1, feedbackId);
                    if (statement.executeUpdate() != 1) {
                        throw new IllegalArgumentException("Feedback not found.");
                    }
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        }
    }

    private List<Feedback> queryFeedback(Long customerId, String search, Long productId, Integer rating,
                                         String sentiment,
                                         LocalDate fromDate, LocalDate toDate, Integer limit)
            throws SQLException {
        StringBuilder sql = new StringBuilder(
                        "SELECT f.feedback_id, f.rating, f.comment_text, f.created_at, "
                        + "COALESCE(v.helpful_votes, 0) AS helpful_votes, "
                        + "COALESCE(v.not_helpful_votes, 0) AS not_helpful_votes, "
                        + "u.full_name AS customer_name, "
                        + "p.product_name, r.response_id, r.response_text, "
                        + "r.created_at AS responded_at, a.full_name AS admin_name "
                        + "FROM feedback f JOIN users u ON u.user_id = f.user_id "
                        + "JOIN products p ON p.product_id = f.product_id "
                        + "LEFT JOIN (SELECT feedback_id, "
                        + "SUM(CASE WHEN vote_type = 'HELPFUL' THEN 1 ELSE 0 END) AS helpful_votes, "
                        + "SUM(CASE WHEN vote_type = 'NOT_HELPFUL' THEN 1 ELSE 0 END) AS not_helpful_votes "
                        + "FROM feedback_votes GROUP BY feedback_id) v ON v.feedback_id = f.feedback_id "
                        + "LEFT JOIN responses r ON r.feedback_id = f.feedback_id "
                        + "LEFT JOIN users a ON a.user_id = r.admin_id WHERE 1 = 1");
        List<Object> parameters = new ArrayList<>();
        if (customerId != null) {
            sql.append(" AND f.user_id = ?");
            parameters.add(customerId);
        }
        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (f.comment_text LIKE ? OR p.product_name LIKE ? OR u.full_name LIKE ?)");
            String pattern = "%" + search.trim() + "%";
            parameters.add(pattern);
            parameters.add(pattern);
            parameters.add(pattern);
        }
        if (productId != null) {
            sql.append(" AND f.product_id = ?");
            parameters.add(productId);
        }
        if (rating != null) {
            if (rating < 1 || rating > 5) {
                throw new IllegalArgumentException("Rating filter must be from 1 to 5.");
            }
            sql.append(" AND f.rating = ?");
            parameters.add(rating);
        }
        if (sentiment != null && !sentiment.trim().isEmpty()) {
            switch (sentiment.toLowerCase(java.util.Locale.ROOT)) {
                case "positive":
                    sql.append(" AND f.rating >= 4");
                    break;
                case "neutral":
                    sql.append(" AND f.rating = 3");
                    break;
                case "negative":
                    sql.append(" AND f.rating <= 2");
                    break;
                default:
                    throw new IllegalArgumentException("Choose a valid sentiment filter.");
            }
        }
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("The start date must be on or before the end date.");
        }
        if (fromDate != null) {
            sql.append(" AND f.created_at >= ?");
            parameters.add(Timestamp.valueOf(fromDate.atStartOfDay()));
        }
        if (toDate != null) {
            sql.append(" AND f.created_at < ?");
            parameters.add(Timestamp.valueOf(toDate.plusDays(1).atStartOfDay()));
        }
        sql.append(" ORDER BY f.created_at DESC");
        if (limit != null) {
            sql.append(" LIMIT ?");
            parameters.add(limit);
        }

        List<Feedback> feedbackList = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int index = 0; index < parameters.size(); index++) {
                statement.setObject(index + 1, parameters.get(index));
            }
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Response response = null;
                    long responseId = result.getLong("response_id");
                    if (!result.wasNull()) {
                        Timestamp respondedAt = result.getTimestamp("responded_at");
                        response = new Response(responseId, result.getLong("feedback_id"),
                                result.getString("admin_name"), result.getString("response_text"),
                                respondedAt == null ? null : respondedAt.toLocalDateTime());
                    }
                    Timestamp createdAt = result.getTimestamp("created_at");
                    feedbackList.add(new Feedback(result.getLong("feedback_id"), result.getString("customer_name"),
                            result.getString("product_name"), result.getInt("rating"), result.getString("comment_text"),
                            createdAt == null ? null : createdAt.toLocalDateTime(), response,
                            result.getLong("helpful_votes"), result.getLong("not_helpful_votes")));
                }
            }
        }
        return feedbackList;
    }

    private static int requireLimit(int limit) {
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("Feedback limit must be between 1 and 100.");
        }
        return limit;
    }

    private static String requireText(String value, String label, int maxLength) {
        if (value == null || value.trim().isEmpty() || value.trim().length() > maxLength) {
            throw new IllegalArgumentException(label + " is required and must be at most " + maxLength + " characters.");
        }
        return value.trim();
    }
}
