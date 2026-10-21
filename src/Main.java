import model.Admin;
import model.Customer;
import model.Feedback;
import model.Product;
import model.User;
import service.AuthService;
import service.FeedbackManager;
import service.ProductService;
import service.ResponseService;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private final Scanner scanner = new Scanner(System.in);
    private final AuthService authService = new AuthService();
    private final ProductService productService = new ProductService();
    private final FeedbackManager feedbackManager = new FeedbackManager();
    private final ResponseService responseService = new ResponseService();

    public static void main(String[] args) {
        try {
            new Main().run();
        } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
            System.err.println("Application error: " + e.getMessage());
        }
    }

    private void run() throws SQLException {
        while (true) {
            System.out.println("\n=== Customer Feedback System ===");
            System.out.println("1. Register");
            System.out.println("2. Log in");
            System.out.println("0. Exit");
            int choice = readInt("Choose: ");
            if (choice == 0) {
                return;
            }
            if (choice == 1) {
                register();
            } else if (choice == 2) {
                User user = login();
                if (user instanceof Admin) {
                    adminMenu((Admin) user);
                } else if (user instanceof Customer) {
                    customerMenu((Customer) user);
                }
            } else {
                System.out.println("Choose one of the listed options.");
            }
        }
    }

    private void register() throws SQLException {
        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        char[] password = readPassword("Password (8-128 characters): ");
        Customer customer = authService.register(name, email, password);
        System.out.println("Account created. Welcome, " + customer.getName() + ".");
    }

    private User login() throws SQLException {
        System.out.print("Email: ");
        String email = scanner.nextLine();
        char[] password = readPassword("Password: ");
        User user = authService.login(email, password);
        System.out.println("Logged in as " + user.getName() + " (" + user.getRole() + ").");
        return user;
    }

    private void customerMenu(Customer customer) throws SQLException {
        while (true) {
            System.out.println("\n=== Customer Menu ===");
            System.out.println("1. View products");
            System.out.println("2. Submit feedback");
            System.out.println("3. View/search my feedback");
            System.out.println("0. Log out");
            int choice = readInt("Choose: ");
            if (choice == 0) {
                return;
            } else if (choice == 1) {
                showProducts();
            } else if (choice == 2) {
                submitFeedback(customer);
            } else if (choice == 3) {
                showFeedback(feedbackManager.listCustomerFeedback(customer, readSearch(), readProductFilter(),
                        readRatingFilter()), false);
            } else {
                System.out.println("Choose one of the listed options.");
            }
        }
    }

    private void adminMenu(Admin admin) throws SQLException {
        while (true) {
            System.out.println("\n=== Administrator Menu ===");
            System.out.println("1. View/search/filter feedback");
            System.out.println("2. Delete feedback");
            System.out.println("3. Respond to feedback");
            System.out.println("4. Product statistics");
            System.out.println("0. Log out");
            int choice = readInt("Choose: ");
            if (choice == 0) {
                return;
            } else if (choice == 1) {
                showFeedback(feedbackManager.listAllFeedback(admin, readSearch(), readProductFilter(),
                        readRatingFilter()), true);
            } else if (choice == 2) {
                feedbackManager.deleteFeedback(admin, readLong("Feedback ID to delete: "));
                System.out.println("Feedback deleted.");
            } else if (choice == 3) {
                long feedbackId = readLong("Feedback ID to respond to: ");
                System.out.print("Response: ");
                responseService.respond(admin, feedbackId, scanner.nextLine());
                System.out.println("Response saved.");
            } else if (choice == 4) {
                showStatistics(admin);
            } else {
                System.out.println("Choose one of the listed options.");
            }
        }
    }

    private void showProducts() throws SQLException {
        List<Product> products = productService.listProducts();
        if (products.isEmpty()) {
            System.out.println("No products are currently available.");
        }
        for (Product product : products) {
            System.out.printf("%d. %s - %s (price: %s)%n",
                    product.getId(), product.getName(), product.getDescription(), product.getPrice());
        }
    }

    private void submitFeedback(Customer customer) throws SQLException {
        showProducts();
        long productId = readLong("Product ID: ");
        int rating = readInt("Rating (1-5): ");
        System.out.print("Comment: ");
        feedbackManager.submit(customer, productId, rating, scanner.nextLine());
        System.out.println("Feedback submitted.");
    }

    private String readSearch() {
        System.out.print("Search text (leave blank for all): ");
        return scanner.nextLine();
    }

    private Long readProductFilter() throws SQLException {
        showProducts();
        System.out.print("Filter by product ID (leave blank for all): ");
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? null : parseLong(input, "Product ID");
    }

    private Integer readRatingFilter() {
        System.out.print("Filter by rating 1-5 (leave blank for all): ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return null;
        }
        int rating = parseInt(input, "Rating");
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating filter must be from 1 to 5.");
        }
        return rating;
    }

    private void showFeedback(List<Feedback> feedbackList, boolean showCustomers) {
        if (feedbackList.isEmpty()) {
            System.out.println("No feedback matched.");
            return;
        }
        for (Feedback feedback : feedbackList) {
            System.out.printf("%nID: %d | Product: %s | Rating: %d/5 | Date: %s%n",
                    feedback.getId(), feedback.getProductName(), feedback.getRating(), feedback.getCreatedAt());
            if (showCustomers) {
                System.out.println("Customer: " + feedback.getCustomerName());
            }
            System.out.println("Comment: " + feedback.getComment());
            if (feedback.getResponse() != null) {
                System.out.printf("Admin response (%s, %s): %s%n", feedback.getResponse().getAdminName(),
                        feedback.getResponse().getRespondedAt(), feedback.getResponse().getText());
            }
        }
    }

    private void showStatistics(Admin admin) throws SQLException {
        for (ProductService.ProductStats stats : productService.getStatistics(admin)) {
            System.out.printf("%s: %d feedback, average rating %.2f/5%n",
                    stats.getProductName(), stats.getFeedbackCount(), stats.getAverageRating());
        }
    }

    private char[] readPassword(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().toCharArray();
    }

    private int readInt(String prompt) {
        return parseInt(readValue(prompt), "Number");
    }

    private long readLong(String prompt) {
        return parseLong(readValue(prompt), "Number");
    }

    private String readValue(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private int parseInt(String value, String label) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " must be a whole number.");
        }
    }

    private long parseLong(String value, String label) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " must be a whole number.");
        }
    }
}
