import model.Admin;
import model.Customer;
import model.Feedback;
import model.Product;
import model.User;
import service.AuthService;
import service.FeedbackManager;
import service.ProductService;
import service.ResponseService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class Main extends JFrame {
    private static final Color NAVY = new Color(22, 35, 57);
    private static final Color NAVY_LIGHT = new Color(35, 52, 79);
    private static final Color BLUE = new Color(57, 105, 229);
    private static final Color PAGE = new Color(244, 247, 251);
    private static final Color TEXT = new Color(37, 48, 65);
    private static final Color MUTED = new Color(111, 124, 144);
    private static final Color BORDER = new Color(225, 231, 239);
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, yyyy  ·  h:mm a");

    private final AuthService authService = new AuthService();
    private final ProductService productService = new ProductService();
    private final FeedbackManager feedbackManager = new FeedbackManager();
    private final ResponseService responseService = new ResponseService();

    private User currentUser;
    private List<Product> products;
    private JPanel pageCards;
    private CardLayout pageLayout;
    private JLabel pageTitle;
    private JLabel pageSubtitle;
    private JLabel statusLabel;
    private JComboBox<ProductOption> submitProduct;
    private JComboBox<ProductOption> filterProduct;
    private JComboBox<String> filterRating;
    private JTextField feedbackSearch;
    private JTextArea feedbackComment;
    private JPanel feedbackResults;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ToolTip.background", NAVY);
            UIManager.put("ToolTip.foreground", Color.WHITE);
            Main app = new Main();
            app.setVisible(true);
        });
    }

    public Main() {
        super("Apex Feedback | Customer Feedback");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 640));
        setSize(1180, 790);
        setLocationRelativeTo(null);
        getContentPane().setBackground(PAGE);
        setContentPane(buildAuthScreen());
    }

    private JPanel buildAuthScreen() {
        JPanel root = new JPanel(new GridLayout(1, 2));
        root.setBackground(Color.WHITE);

        JPanel brand = new JPanel();
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBackground(NAVY);
        brand.setBorder(BorderFactory.createEmptyBorder(76, 64, 64, 54));
        JLabel brandName = label("APEX  /  FEEDBACK", 14, new Color(174, 196, 255), Font.BOLD);
        JLabel headline = label("<html>Every voice<br>makes us better.</html>", 38, Color.WHITE, Font.BOLD);
        JLabel description = label(
                "<html>One thoughtful place to share product experiences, "
                        + "track conversations, and turn feedback into progress.</html>",
                16, new Color(197, 207, 222), Font.PLAIN);
        description.setMaximumSize(new Dimension(390, 90));
        brand.add(brandName);
        brand.add(Box.createVerticalStrut(44));
        brand.add(headline);
        brand.add(Box.createVerticalStrut(22));
        brand.add(description);
        brand.add(Box.createVerticalGlue());
        brand.add(label("A clearer conversation between customers and teams.", 12,
                new Color(154, 170, 193), Font.PLAIN));

        JPanel auth = new JPanel(new BorderLayout());
        auth.setBackground(Color.WHITE);
        auth.setBorder(BorderFactory.createEmptyBorder(60, 60, 54, 60));
        JPanel authContent = new JPanel(new BorderLayout(0, 22));
        authContent.setBackground(Color.WHITE);
        JLabel heading = label("Welcome", 30, TEXT, Font.BOLD);
        JLabel subheading = label("Sign in or create an account to continue.", 14, MUTED, Font.PLAIN);
        JPanel intro = new JPanel();
        intro.setBackground(Color.WHITE);
        intro.setLayout(new BoxLayout(intro, BoxLayout.Y_AXIS));
        intro.add(heading);
        intro.add(Box.createVerticalStrut(8));
        intro.add(subheading);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.setForeground(TEXT);
        tabs.addTab("Sign in", buildLoginForm());
        tabs.addTab("Create account", buildRegisterForm());
        authContent.add(intro, BorderLayout.NORTH);
        authContent.add(tabs, BorderLayout.CENTER);
        auth.add(authContent, BorderLayout.CENTER);
        root.add(brand);
        root.add(auth);
        return root;
    }

    private JPanel buildLoginForm() {
        JPanel form = formPanel();
        JTextField email = new JTextField();
        JPasswordField password = new JPasswordField();
        JButton signIn = primaryButton("Sign in");
        form.add(fieldBlock("Email address", email));
        form.add(Box.createVerticalStrut(18));
        form.add(fieldBlock("Password", password));
        form.add(Box.createVerticalStrut(26));
        form.add(signIn);
        form.add(Box.createVerticalGlue());
        signIn.addActionListener(event -> {
            String enteredEmail = email.getText();
            char[] enteredPassword = password.getPassword();
            runTask("Signing in…", () -> authService.login(enteredEmail, enteredPassword), this::openDashboard);
        });
        password.addActionListener(event -> signIn.doClick());
        return form;
    }

    private JPanel buildRegisterForm() {
        JPanel form = formPanel();
        JTextField name = new JTextField();
        JTextField email = new JTextField();
        JPasswordField password = new JPasswordField();
        JButton create = primaryButton("Create account");
        form.add(fieldBlock("Full name", name));
        form.add(Box.createVerticalStrut(12));
        form.add(fieldBlock("Email address", email));
        form.add(Box.createVerticalStrut(12));
        form.add(fieldBlock("Password", password));
        form.add(Box.createVerticalStrut(7));
        form.add(label("Use 8–128 characters.", 12, MUTED, Font.PLAIN));
        form.add(Box.createVerticalStrut(18));
        form.add(create);
        create.addActionListener(event -> {
            String enteredName = name.getText();
            String enteredEmail = email.getText();
            char[] enteredPassword = password.getPassword();
            runTask("Creating your account…",
                    () -> authService.register(enteredName, enteredEmail, enteredPassword),
                    customer -> {
                        JOptionPane.showMessageDialog(this,
                                "Your account is ready, " + customer.getName() + ". You can now sign in.",
                                "Account created", JOptionPane.INFORMATION_MESSAGE);
                    });
        });
        return form;
    }

    private void openDashboard(User user) {
        currentUser = user;
        statusLabel = new JLabel("Loading your workspace…");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(MUTED);
        runTask("Loading your workspace…", productService::listProducts, loadedProducts -> {
            products = loadedProducts;
            setContentPane(buildDashboard());
            revalidate();
            repaint();
            status("Ready");
            showPage("overview");
        });
    }

    private JPanel buildDashboard() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(PAGE);
        root.add(buildSidebar(), BorderLayout.WEST);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(PAGE);
        content.setBorder(BorderFactory.createEmptyBorder(28, 34, 24, 34));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        pageTitle = label("Overview", 25, TEXT, Font.BOLD);
        pageSubtitle = label("A quick look at your feedback workspace.", 13, MUTED, Font.PLAIN);
        titles.add(pageTitle);
        titles.add(Box.createVerticalStrut(5));
        titles.add(pageSubtitle);
        header.add(titles, BorderLayout.WEST);
        statusLabel = label("Ready", 12, MUTED, Font.PLAIN);
        header.add(statusLabel, BorderLayout.EAST);
        content.add(header, BorderLayout.NORTH);

        pageLayout = new CardLayout();
        pageCards = new JPanel(pageLayout);
        pageCards.setOpaque(false);
        pageCards.setBorder(BorderFactory.createEmptyBorder(24, 0, 0, 0));
        pageCards.add(buildOverviewPage(), "overview");
        if (currentUser instanceof Admin) {
            pageCards.add(buildFeedbackPage(true), "feedback");
            pageCards.add(buildStatisticsPage(), "statistics");
        } else {
            pageCards.add(buildProductsPage(), "products");
            pageCards.add(buildSubmitPage(), "submit");
            pageCards.add(buildFeedbackPage(false), "feedback");
        }
        content.add(pageCards, BorderLayout.CENTER);
        root.add(content, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(238, 0));
        sidebar.setBackground(NAVY);
        sidebar.setBorder(BorderFactory.createEmptyBorder(27, 18, 22, 18));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JLabel logo = label("APEX", 21, Color.WHITE, Font.BOLD);
        JLabel logoSub = label("FEEDBACK", 10, new Color(159, 181, 225), Font.BOLD);
        sidebar.add(Box.createHorizontalStrut(1));
        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(2));
        sidebar.add(logoSub);
        sidebar.add(Box.createVerticalStrut(32));

        String role = currentUser instanceof Admin ? "ADMIN WORKSPACE" : "CUSTOMER WORKSPACE";
        JLabel roleLabel = label(role, 10, new Color(145, 164, 192), Font.BOLD);
        sidebar.add(roleLabel);
        sidebar.add(Box.createVerticalStrut(12));
        addNavigation(sidebar, "Overview", "overview");
        if (currentUser instanceof Admin) {
            addNavigation(sidebar, "All feedback", "feedback");
            addNavigation(sidebar, "Product insights", "statistics");
        } else {
            addNavigation(sidebar, "Browse products", "products");
            addNavigation(sidebar, "Write feedback", "submit");
            addNavigation(sidebar, "My feedback", "feedback");
        }

        sidebar.add(Box.createVerticalGlue());
        JPanel profile = new JPanel();
        profile.setOpaque(false);
        profile.setLayout(new BoxLayout(profile, BoxLayout.Y_AXIS));
        profile.setBorder(BorderFactory.createEmptyBorder(12, 10, 14, 8));
        profile.add(label(currentUser.getName(), 14, Color.WHITE, Font.BOLD));
        profile.add(Box.createVerticalStrut(4));
        profile.add(label(currentUser.getRole().toLowerCase(), 12,
                new Color(167, 183, 205), Font.PLAIN));
        sidebar.add(profile);
        JButton logout = navButton("Sign out");
        logout.addActionListener(event -> {
            currentUser = null;
            products = null;
            setContentPane(buildAuthScreen());
            revalidate();
            repaint();
        });
        sidebar.add(logout);
        return sidebar;
    }

    private void addNavigation(JPanel sidebar, String text, String page) {
        JButton button = navButton(text);
        button.addActionListener(event -> showPage(page));
        sidebar.add(button);
        sidebar.add(Box.createVerticalStrut(5));
    }

    private JButton navButton(String text) {
        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(new Color(220, 228, 240));
        button.setBackground(NAVY);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 10));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent event) {
                button.setBackground(NAVY_LIGHT);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent event) {
                button.setBackground(NAVY);
            }
        });
        return button;
    }

    private JPanel buildOverviewPage() {
        JPanel panel = verticalPanel();
        JPanel hero = new JPanel(new BorderLayout(24, 0));
        hero.setBackground(NAVY_LIGHT);
        hero.setBorder(BorderFactory.createEmptyBorder(30, 32, 30, 32));
        JPanel heroText = new JPanel();
        heroText.setOpaque(false);
        heroText.setLayout(new BoxLayout(heroText, BoxLayout.Y_AXIS));
        String greeting = currentUser instanceof Admin ? "Your feedback command center"
                : "Your experience matters";
        heroText.add(label(greeting, 23, Color.WHITE, Font.BOLD));
        heroText.add(Box.createVerticalStrut(10));
        String message = currentUser instanceof Admin
                ? "Review customer voices, respond to conversations, and keep an eye on product trends."
                : "Explore products, share what you think, and follow the conversation with our team.";
        JLabel description = label("<html>" + message + "</html>", 14,
                new Color(213, 222, 235), Font.PLAIN);
        description.setMaximumSize(new Dimension(600, 50));
        heroText.add(description);
        hero.add(heroText, BorderLayout.CENTER);
        panel.add(hero);
        panel.add(Box.createVerticalStrut(20));

        JPanel cards = new JPanel(new GridLayout(1, 3, 14, 0));
        cards.setOpaque(false);
        cards.add(metricCard("YOUR ROLE", currentUser.getRole(), "Workspace access"));
        cards.add(metricCard("AVAILABLE PRODUCTS", String.valueOf(products.size()), "Ready to explore"));
        cards.add(metricCard("YOUR ACCOUNT", "Active", currentUser.getEmail()));
        panel.add(cards);
        panel.add(Box.createVerticalStrut(20));

        JPanel next = cardPanel();
        next.setLayout(new BoxLayout(next, BoxLayout.Y_AXIS));
        next.add(label(currentUser instanceof Admin ? "Next steps" : "Get started", 17, TEXT, Font.BOLD));
        next.add(Box.createVerticalStrut(10));
        next.add(label(currentUser instanceof Admin
                ? "Open All feedback to search, filter, respond to, or remove a feedback item."
                : "Open Browse products to discover what is available, then use Write feedback to share your thoughts.",
                13, MUTED, Font.PLAIN));
        panel.add(next);
        return panel;
    }

    private JPanel buildProductsPage() {
        JPanel panel = verticalPanel();
        JPanel intro = cardPanel();
        intro.setLayout(new BorderLayout());
        intro.add(label("Find a product to review", 17, TEXT, Font.BOLD), BorderLayout.NORTH);
        intro.add(label("Browse the catalog and share your experience with the community.",
                13, MUTED, Font.PLAIN), BorderLayout.CENTER);
        panel.add(intro);
        panel.add(Box.createVerticalStrut(14));

        JPanel list = verticalPanel();
        if (products.isEmpty()) {
            list.add(emptyState("No products are available right now."));
        } else {
            for (Product product : products) {
                JPanel productCard = cardPanel();
                productCard.setLayout(new BorderLayout(16, 8));
                JPanel details = new JPanel();
                details.setOpaque(false);
                details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
                details.add(label(product.getName(), 17, TEXT, Font.BOLD));
                details.add(Box.createVerticalStrut(7));
                details.add(label(product.getDescription(), 13, MUTED, Font.PLAIN));
                productCard.add(details, BorderLayout.CENTER);
                JLabel price = label("$" + product.getPrice().toPlainString(), 15, BLUE, Font.BOLD);
                productCard.add(price, BorderLayout.EAST);
                panel.add(productCard);
                panel.add(Box.createVerticalStrut(10));
            }
        }
        JScrollPane scroll = scrollPane(list);
        panel.add(scroll);
        return panel;
    }

    private JPanel buildSubmitPage() {
        JPanel page = verticalPanel();
        JPanel form = cardPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(label("Tell us what you think", 18, TEXT, Font.BOLD));
        form.add(Box.createVerticalStrut(6));
        form.add(label("Your feedback helps us understand what is working and what could be better.",
                13, MUTED, Font.PLAIN));
        form.add(Box.createVerticalStrut(22));

        submitProduct = productCombo(false);
        JComboBox<Integer> rating = new JComboBox<>(new Integer[]{5, 4, 3, 2, 1});
        rating.setRenderer((list, value, index, selected, focus) ->
                new JLabel(value == null ? "" : value + " / 5  " + stars(value)));
        feedbackComment = new JTextArea(7, 30);
        styleTextArea(feedbackComment);
        feedbackComment.setLineWrap(true);
        feedbackComment.setWrapStyleWord(true);

        form.add(fieldBlock("Product", submitProduct));
        form.add(Box.createVerticalStrut(16));
        form.add(fieldBlock("Your rating", rating));
        form.add(Box.createVerticalStrut(16));
        form.add(fieldBlock("Comment (up to 500 characters)", scrollPane(feedbackComment)));
        form.add(Box.createVerticalStrut(18));
        JButton submit = primaryButton("Submit feedback");
        submit.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(submit);
        submit.addActionListener(event -> {
            ProductOption selected = (ProductOption) submitProduct.getSelectedItem();
            if (selected == null || selected.product == null) {
                showError("Select a product before submitting feedback.");
                return;
            }
            Product product = selected.product;
            int selectedRating = (Integer) rating.getSelectedItem();
            String comment = feedbackComment.getText();
            runTask("Submitting feedback…", () -> {
                feedbackManager.submit((Customer) currentUser, product.getId(), selectedRating, comment);
                return null;
            }, ignored -> {
                feedbackComment.setText("");
                JOptionPane.showMessageDialog(this, "Thanks for sharing your feedback.",
                        "Feedback submitted", JOptionPane.INFORMATION_MESSAGE);
                status("Feedback submitted");
            });
        });
        page.add(form);
        return page;
    }

    private JPanel buildFeedbackPage(boolean admin) {
        JPanel page = new JPanel(new BorderLayout(0, 14));
        page.setOpaque(false);
        JPanel filters = cardPanel();
        filters.setLayout(new BorderLayout(12, 0));
        JPanel controls = new JPanel(new GridLayout(1, 3, 10, 0));
        controls.setOpaque(false);
        feedbackSearch = new JTextField();
        feedbackSearch.setToolTipText("Search comments, products, and customer names");
        filterProduct = productCombo(true);
        filterRating = new JComboBox<>(new String[]{"Any rating", "5 stars", "4 stars", "3 stars", "2 stars", "1 star"});
        controls.add(feedbackSearch);
        controls.add(filterProduct);
        controls.add(filterRating);
        JButton search = primaryButton("Search");
        filters.add(controls, BorderLayout.CENTER);
        filters.add(search, BorderLayout.EAST);
        page.add(filters, BorderLayout.NORTH);

        feedbackResults = verticalPanel();
        page.add(scrollPane(feedbackResults), BorderLayout.CENTER);
        search.addActionListener(event -> loadFeedback(admin));
        feedbackSearch.addActionListener(event -> loadFeedback(admin));
        loadFeedback(admin);
        return page;
    }

    private JPanel buildStatisticsPage() {
        JPanel page = new JPanel(new BorderLayout(0, 14));
        page.setOpaque(false);
        JPanel heading = cardPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(label("Product performance", 17, TEXT, Font.BOLD));
        heading.add(Box.createVerticalStrut(6));
        heading.add(label("A summary of customer feedback and average product ratings.",
                13, MUTED, Font.PLAIN));
        page.add(heading, BorderLayout.NORTH);
        String[] columns = {"Product", "Feedback count", "Average rating"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(42);
        table.setForeground(TEXT);
        table.setGridColor(BORDER);
        table.setSelectionBackground(new Color(231, 238, 252));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(249, 250, 252));
        table.getTableHeader().setForeground(MUTED);
        JPanel tableCard = cardPanel();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);
        page.add(tableCard, BorderLayout.CENTER);
        runTask("Loading product insights…", () -> productService.getStatistics((Admin) currentUser), stats -> {
            model.setRowCount(0);
            for (ProductService.ProductStats stat : stats) {
                model.addRow(new Object[]{stat.getProductName(), stat.getFeedbackCount(),
                        String.format("%.2f / 5", stat.getAverageRating())});
            }
            if (stats.isEmpty()) {
                status("No product statistics available");
            } else {
                status("Insights updated");
            }
        });
        return page;
    }

    private void loadFeedback(boolean admin) {
        String search = feedbackSearch.getText();
        ProductOption option = (ProductOption) filterProduct.getSelectedItem();
        Long productId = option == null || option.product == null ? null : option.product.getId();
        int ratingIndex = filterRating.getSelectedIndex();
        Integer rating = ratingIndex <= 0 ? null : 6 - ratingIndex;
        runTask("Loading feedback…", () -> admin
                        ? feedbackManager.listAllFeedback((Admin) currentUser, search, productId, rating)
                        : feedbackManager.listCustomerFeedback((Customer) currentUser, search, productId, rating),
                results -> renderFeedback(results, admin));
    }

    private void renderFeedback(List<Feedback> results, boolean admin) {
        feedbackResults.removeAll();
        if (results.isEmpty()) {
            feedbackResults.add(emptyState("No feedback matched your filters."));
        } else {
            for (Feedback feedback : results) {
                feedbackResults.add(feedbackCard(feedback, admin));
                feedbackResults.add(Box.createVerticalStrut(12));
            }
        }
        feedbackResults.revalidate();
        feedbackResults.repaint();
        status(results.size() + (results.size() == 1 ? " feedback item" : " feedback items"));
    }

    private JPanel feedbackCard(Feedback feedback, boolean admin) {
        JPanel card = cardPanel();
        card.setLayout(new BorderLayout(0, 12));
        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setOpaque(false);
        JPanel title = new JPanel();
        title.setOpaque(false);
        title.setLayout(new BoxLayout(title, BoxLayout.Y_AXIS));
        title.add(label(feedback.getProductName(), 16, TEXT, Font.BOLD));
        String submitted = formatDate(feedback.getCreatedAt());
        title.add(Box.createVerticalStrut(5));
        title.add(label((admin ? feedback.getCustomerName() + "  ·  " : "")
                + "Feedback #" + feedback.getId() + "  ·  " + submitted, 12, MUTED, Font.PLAIN));
        top.add(title, BorderLayout.CENTER);
        top.add(label(stars(feedback.getRating()) + "  " + feedback.getRating() + "/5",
                14, new Color(190, 127, 26), Font.BOLD), BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JTextArea comment = new JTextArea(feedback.getComment());
        styleTextArea(comment);
        comment.setLineWrap(true);
        comment.setWrapStyleWord(true);
        comment.setEditable(false);
        comment.setBackground(new Color(248, 250, 253));
        comment.setBorder(BorderFactory.createEmptyBorder(11, 12, 11, 12));
        content.add(comment);
        if (feedback.getResponse() != null) {
            JPanel response = new JPanel();
            response.setLayout(new BoxLayout(response, BoxLayout.Y_AXIS));
            response.setBackground(new Color(239, 245, 255));
            response.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 0, 0, BLUE),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)));
            response.add(label("TEAM RESPONSE  ·  " + feedback.getResponse().getAdminName(), 10,
                    BLUE, Font.BOLD));
            response.add(Box.createVerticalStrut(5));
            response.add(label("<html>" + escapeHtml(feedback.getResponse().getText()) + "</html>",
                    13, TEXT, Font.PLAIN));
            content.add(Box.createVerticalStrut(9));
            content.add(response);
        }
        card.add(content, BorderLayout.CENTER);

        if (admin) {
            JPanel actions = new JPanel(new GridLayout(1, 2, 8, 0));
            actions.setOpaque(false);
            JButton respond = secondaryButton(feedback.getResponse() == null ? "Respond" : "Edit response");
            JButton delete = dangerButton("Delete");
            respond.addActionListener(event -> promptResponse(feedback));
            delete.addActionListener(event -> confirmDelete(feedback));
            actions.add(respond);
            actions.add(delete);
            card.add(actions, BorderLayout.SOUTH);
        }
        return card;
    }

    private void promptResponse(Feedback feedback) {
        JTextArea responseText = new JTextArea(6, 34);
        styleTextArea(responseText);
        responseText.setLineWrap(true);
        responseText.setWrapStyleWord(true);
        if (feedback.getResponse() != null) {
            responseText.setText(feedback.getResponse().getText());
        }
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(label("Response for " + feedback.getProductName() + " · Feedback #"
                + feedback.getId(), 13, TEXT, Font.BOLD), BorderLayout.NORTH);
        panel.add(scrollPane(responseText), BorderLayout.CENTER);
        int choice = JOptionPane.showConfirmDialog(this, panel, "Respond to feedback",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice == JOptionPane.OK_OPTION) {
            String text = responseText.getText();
            runTask("Saving response…", () -> {
                responseService.respond((Admin) currentUser, feedback.getId(), text);
                return null;
            }, ignored -> loadFeedback(true));
        }
    }

    private void confirmDelete(Feedback feedback) {
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete feedback #" + feedback.getId() + " and its response? This cannot be undone.",
                "Delete feedback", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            runTask("Deleting feedback…", () -> {
                feedbackManager.deleteFeedback((Admin) currentUser, feedback.getId());
                return null;
            }, ignored -> loadFeedback(true));
        }
    }

    private void showPage(String page) {
        if (pageCards == null) {
            return;
        }
        pageLayout.show(pageCards, page);
        switch (page) {
            case "overview":
                setPageHeading("Overview", "A quick look at your feedback workspace.");
                break;
            case "products":
                setPageHeading("Browse products", "Discover the products available for customer feedback.");
                break;
            case "submit":
                setPageHeading("Write feedback", "Share a rating and comment with the team.");
                break;
            case "feedback":
                setPageHeading(currentUser instanceof Admin ? "All feedback" : "My feedback",
                        currentUser instanceof Admin ? "Search, filter, and manage customer conversations."
                                : "Review your submissions and team responses.");
                if (feedbackSearch != null) {
                    loadFeedback(currentUser instanceof Admin);
                }
                break;
            case "statistics":
                setPageHeading("Product insights", "Understand ratings and feedback volume at a glance.");
                break;
            default:
                throw new IllegalArgumentException("Unknown page: " + page);
        }
    }

    private void setPageHeading(String title, String subtitle) {
        pageTitle.setText(title);
        pageSubtitle.setText(subtitle);
    }

    private JComboBox<ProductOption> productCombo(boolean includeAll) {
        JComboBox<ProductOption> combo = new JComboBox<>();
        if (includeAll) {
            combo.addItem(new ProductOption(null, "All products"));
        }
        for (Product product : products) {
            combo.addItem(new ProductOption(product, product.getName()));
        }
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setPreferredSize(new Dimension(180, 40));
        return combo;
    }

    private <T> void runTask(String busyMessage, Callable<T> task, Consumer<T> success) {
        status(busyMessage);
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return task.call();
            }

            @Override
            protected void done() {
                try {
                    T result = get();
                    success.accept(result);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    status("Operation interrupted");
                    showError("The operation was interrupted. Please try again.");
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    status("Action failed");
                    showError(cause == null || cause.getMessage() == null
                            ? "The operation could not be completed." : cause.getMessage());
                }
            }
        }.execute();
    }

    private void status(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Something went wrong", JOptionPane.ERROR_MESSAGE);
    }

    private JPanel metricCard(String title, String value, String detail) {
        JPanel card = cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.add(label(title, 10, MUTED, Font.BOLD));
        card.add(Box.createVerticalStrut(10));
        card.add(label(value, 22, TEXT, Font.BOLD));
        card.add(Box.createVerticalStrut(6));
        card.add(label(detail, 11, MUTED, Font.PLAIN));
        return card;
    }

    private JPanel fieldBlock(String title, Component field) {
        JPanel block = new JPanel();
        block.setBackground(Color.WHITE);
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        JLabel label = label(title, 12, TEXT, Font.BOLD);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        if (field instanceof JTextField) {
            ((JTextField) field).setPreferredSize(new Dimension(320, 42));
            ((JTextField) field).setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            ((JTextField) field).setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER),
                    BorderFactory.createEmptyBorder(8, 11, 8, 11)));
        } else if (field instanceof JComboBox) {
            field.setPreferredSize(new Dimension(320, 42));
            field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        }
        if (field instanceof JComponent) {
            ((JComponent) field).setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        block.add(label);
        block.add(Box.createVerticalStrut(7));
        block.add(field);
        block.setAlignmentX(Component.LEFT_ALIGNMENT);
        return block;
    }

    private JPanel formPanel() {
        JPanel form = new JPanel();
        form.setBackground(Color.WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(24, 8, 12, 8));
        return form;
    }

    private JPanel verticalPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    private JPanel cardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(20, 22, 20, 22)));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private JPanel emptyState(String message) {
        JPanel panel = cardPanel();
        panel.setLayout(new BorderLayout());
        JLabel text = label(message, 14, MUTED, Font.PLAIN);
        text.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(text, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane scrollPane(Component component) {
        JScrollPane scroll = new JScrollPane(component);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(PAGE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(BLUE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(11, 18, 11, 18));
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        return button;
    }

    private JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(BLUE);
        button.setBackground(new Color(239, 244, 255));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));
        return button;
    }

    private JButton dangerButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(new Color(176, 55, 55));
        button.setBackground(new Color(255, 242, 241));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));
        return button;
    }

    private JLabel label(String text, int size, Color color, int style) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", style, size));
        label.setForeground(color);
        return label;
    }

    private void styleTextArea(JTextArea area) {
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setForeground(TEXT);
        area.setMargin(new Insets(8, 10, 8, 10));
        area.setBorder(BorderFactory.createLineBorder(BORDER));
    }

    private String formatDate(LocalDateTime date) {
        return date == null ? "Date unavailable" : DATE_FORMAT.format(date);
    }

    private String stars(int rating) {
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            value.append(i < rating ? "★" : "☆");
        }
        return value.toString();
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\n", "<br>");
    }

    private static final class ProductOption {
        private final Product product;
        private final String label;

        private ProductOption(Product product, String label) {
            this.product = product;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
