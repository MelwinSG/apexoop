package service;

import database.DBConnection;
import model.Admin;
import model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductService {
    public List<Product> listProducts() throws SQLException {
        String sql = "SELECT product_id, product_name, description, price FROM products ORDER BY product_name";
        List<Product> products = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                products.add(new Product(result.getLong("product_id"), result.getString("product_name"),
                        result.getString("description"), result.getBigDecimal("price")));
            }
        }
        return products;
    }

    public List<ProductStats> getStatistics(Admin admin) throws SQLException {
        AuthService.requireAdmin(admin);
        String sql = "SELECT p.product_id, p.product_name, COUNT(f.feedback_id) AS feedback_count, "
                + "COALESCE(AVG(f.rating), 0) AS average_rating "
                + "FROM products p LEFT JOIN feedback f ON f.product_id = p.product_id "
                + "GROUP BY p.product_id, p.product_name ORDER BY p.product_name";
        List<ProductStats> statistics = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                statistics.add(new ProductStats(result.getLong("product_id"), result.getString("product_name"),
                        result.getLong("feedback_count"), result.getDouble("average_rating")));
            }
        }
        return statistics;
    }

    public static class ProductStats {
        private final long productId;
        private final String productName;
        private final long feedbackCount;
        private final double averageRating;

        public ProductStats(long productId, String productName, long feedbackCount, double averageRating) {
            this.productId = productId;
            this.productName = productName;
            this.feedbackCount = feedbackCount;
            this.averageRating = averageRating;
        }

        public long getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public long getFeedbackCount() {
            return feedbackCount;
        }

        public double getAverageRating() {
            return averageRating;
        }
    }
}
