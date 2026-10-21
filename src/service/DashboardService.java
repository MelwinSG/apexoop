package service;

import database.DBConnection;
import model.Admin;
import model.Customer;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class DashboardService {
    public Summary getSummary(User user) throws SQLException {
        String scope = scopeClause(user);
        String sql = "SELECT COUNT(*) AS total_count, "
                + "COALESCE(SUM(CASE WHEN rating >= 4 THEN 1 ELSE 0 END), 0) AS positive_count, "
                + "COALESCE(SUM(CASE WHEN rating = 3 THEN 1 ELSE 0 END), 0) AS neutral_count, "
                + "COALESCE(SUM(CASE WHEN rating <= 2 THEN 1 ELSE 0 END), 0) AS negative_count "
                + "FROM feedback" + scope;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setUserParameter(statement, user);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("Feedback summary was not returned.");
                }
                return new Summary(result.getLong("total_count"), result.getLong("positive_count"),
                        result.getLong("neutral_count"), result.getLong("negative_count"));
            }
        }
    }

    public List<RatingCount> getRatingBreakdown(User user) throws SQLException {
        String scope = scopeClause(user);
        String sql = "SELECT rating, COUNT(*) AS rating_count FROM feedback"
                + scope + " GROUP BY rating ORDER BY rating";
        long[] counts = new long[6];
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setUserParameter(statement, user);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    counts[result.getInt("rating")] = result.getLong("rating_count");
                }
            }
        }
        List<RatingCount> breakdown = new ArrayList<>();
        for (int rating = 5; rating >= 1; rating--) {
            breakdown.add(new RatingCount(rating, counts[rating]));
        }
        return breakdown;
    }

    public List<DailyCount> getDailyTrend(User user) throws SQLException {
        String scope = scopeClause(user);
        String sql = "SELECT DATE(created_at) AS feedback_day, COUNT(*) AS feedback_count "
                + "FROM feedback" + (scope.isEmpty() ? " WHERE " : scope + " AND ")
                + "created_at >= DATE_SUB(CURRENT_DATE, INTERVAL 6 DAY) "
                + "AND created_at < DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY) "
                + "GROUP BY DATE(created_at) ORDER BY feedback_day";
        Map<LocalDate, Long> counts = new HashMap<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setUserParameter(statement, user);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    java.sql.Date date = result.getDate("feedback_day");
                    if (date != null) {
                        counts.put(date.toLocalDate(), result.getLong("feedback_count"));
                    }
                }
            }
        }
        List<DailyCount> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int day = 6; day >= 0; day--) {
            LocalDate date = today.minusDays(day);
            trend.add(new DailyCount(date, counts.containsKey(date) ? counts.get(date) : 0));
        }
        return trend;
    }

    private String scopeClause(User user) throws SQLException {
        if (user instanceof Admin) {
            AuthService.requireAdmin((Admin) user);
            return "";
        }
        if (user instanceof Customer) {
            AuthService.requireCustomer((Customer) user);
            return " WHERE user_id = ?";
        }
        throw new IllegalArgumentException("A signed-in account is required.");
    }

    private void setUserParameter(PreparedStatement statement, User user) throws SQLException {
        if (user instanceof Customer) {
            statement.setLong(1, user.getId());
        }
    }

    public static class Summary {
        private final long total;
        private final long positive;
        private final long neutral;
        private final long negative;

        public Summary(long total, long positive, long neutral, long negative) {
            this.total = total;
            this.positive = positive;
            this.neutral = neutral;
            this.negative = negative;
        }

        public long getTotal() {
            return total;
        }

        public long getPositive() {
            return positive;
        }

        public long getNeutral() {
            return neutral;
        }

        public long getNegative() {
            return negative;
        }
    }

    public static class RatingCount {
        private final int rating;
        private final long count;

        public RatingCount(int rating, long count) {
            this.rating = rating;
            this.count = count;
        }

        public int getRating() {
            return rating;
        }

        public long getCount() {
            return count;
        }
    }

    public static class DailyCount {
        private final LocalDate date;
        private final long count;

        public DailyCount(LocalDate date, long count) {
            this.date = date;
            this.count = count;
        }

        public LocalDate getDate() {
            return date;
        }

        public long getCount() {
            return count;
        }
    }
}
