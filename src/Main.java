import model.Admin;
import model.Customer;
import model.Feedback;
import model.Product;
import model.User;
import service.AuthService;
import service.DashboardService;
import service.FeedbackManager;
import service.ProductService;
import service.ResponseService;
import database.DBConnection;

import javax.swing.BorderFactory;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
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
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.border.AbstractBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.KeyStroke;

public class Main extends JFrame {
    private static final Color NAVY = new Color(22, 30, 51);
    private static final Color NAVY_LIGHT = new Color(39, 51, 80);
    private static final Color BLUE = new Color(80, 103, 232);
    private static final Color CYAN = new Color(63, 181, 199);
    private static final Color PAGE = new Color(245, 247, 251);
    private static final Color TEXT = new Color(34, 43, 63);
    private static final Color MUTED = new Color(112, 122, 143);
    private static final Color BORDER = new Color(226, 230, 239);
    private static final Color PAPER = Color.WHITE;
    private static final Color ACCENT_LIGHT = new Color(237, 240, 255);
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, yyyy  ·  h:mm a");

    private final AuthService authService = new AuthService();
    private final ProductService productService = new ProductService();
    private final DashboardService dashboardService = new DashboardService();
    private final FeedbackManager feedbackManager = new FeedbackManager();
    private final ResponseService responseService = new ResponseService();

    private User currentUser;
    private List<Product> products;
    private JPanel pageCards;
    private CardLayout pageLayout;
    private JLabel pageTitle;
    private JLabel pageSubtitle;
    private JLabel statusLabel;
    private final Map<String, JButton> navigationButtons = new LinkedHashMap<>();
    private String activePage = "dashboard";
    private JComboBox<ProductOption> submitProduct;
    private JComboBox<ProductOption> filterProduct;
    private JComboBox<String> filterRating;
    private JComboBox<String> filterSentiment;
    private JTextField feedbackSearch;
    private JTextField fromDateField;
    private JTextField toDateField;
    private JTextArea feedbackComment;
    private JTable feedbackTable;
    private DefaultTableModel feedbackTableModel;
    private List<Feedback> displayedFeedback;
    private List<Feedback> recentFeedbackData;
    private JTable recentFeedbackTable;
    private DefaultTableModel recentFeedbackModel;
    private JPanel recentTableHolder;
    private JPanel feedbackTableHolder;
    private JPanel analyticsRatingsPanel;
    private JPanel analyticsTrendPanel;
    private DefaultTableModel analyticsProductModel;
    private JLabel[] summaryValues;
    private JLabel databaseStateLabel;
    private JLabel settingsConnectionLabel;
    private JLabel sidebarDatabaseLabel;
    private JTextField globalSearchField;
    private JButton refreshButton;
    private JLabel feedbackPageLabel;
    private JButton previousFeedbackPage;
    private JButton nextFeedbackPage;
    private int feedbackCurrentPage;
    private static final int FEEDBACK_PAGE_SIZE = 20;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ToolTip.background", NAVY);
            UIManager.put("ToolTip.foreground", Color.WHITE);
            UIManager.put("Panel.background", PAGE);
            UIManager.put("TextField.background", PAPER);
            UIManager.put("TextField.foreground", TEXT);
            UIManager.put("ComboBox.background", PAPER);
            UIManager.put("ComboBox.foreground", TEXT);
            Main app = new Main();
            app.setVisible(true);
        });
    }

    public Main() {
        super("Customer Feedback | Feedback Workspace");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 680));
        setSize(1320, 860);
        setLocationRelativeTo(null);
        getContentPane().setBackground(PAGE);
        setContentPane(buildAuthScreen());
    }

    private JPanel buildAuthScreen() {
        JPanel root = new JPanel(new GridLayout(1, 2));
        root.setBackground(PAPER);

        JPanel brand = new JPanel();
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBackground(NAVY);
        brand.setBorder(BorderFactory.createEmptyBorder(76, 64, 64, 54));
        JLabel brandName = label("APEX  /  CUSTOMER FEEDBACK", 13, new Color(166, 183, 255), Font.BOLD);
        JLabel headline = editorialLabel("<html>Every voice<br>makes us better.</html>", 42, Color.WHITE);
        JLabel description = label(
                "<html>One thoughtful place to share product experiences, "
                        + "track conversations, and turn feedback into progress.</html>",
                16, new Color(210, 218, 232), Font.PLAIN);
        description.setMaximumSize(new Dimension(390, 90));
        brand.add(brandName);
        brand.add(Box.createVerticalStrut(44));
        brand.add(headline);
        brand.add(Box.createVerticalStrut(22));
        brand.add(description);
        brand.add(Box.createVerticalGlue());
        brand.add(label("A clearer conversation between customers and teams.", 12,
                new Color(169, 181, 207), Font.PLAIN));

        JPanel auth = new JPanel(new BorderLayout());
        auth.setBackground(PAPER);
        auth.setBorder(BorderFactory.createEmptyBorder(60, 60, 54, 60));
        JPanel authContent = new JPanel(new BorderLayout(0, 22));
        authContent.setBackground(PAPER);
        JLabel heading = editorialLabel("Welcome", 34, TEXT);
        JLabel subheading = label("Sign in or create an account to continue.", 14, MUTED, Font.PLAIN);
        JPanel intro = new JPanel();
        intro.setBackground(PAPER);
        intro.setLayout(new BoxLayout(intro, BoxLayout.Y_AXIS));
        intro.add(heading);
        intro.add(Box.createVerticalStrut(8));
        intro.add(subheading);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.setForeground(TEXT);
        tabs.setBackground(PAPER);
        tabs.setBorder(BorderFactory.createEmptyBorder());
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
            showPage("dashboard");
        });
    }

    private JPanel buildDashboard() {
        navigationButtons.clear();
        activePage = "dashboard";
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(PAGE);
        root.add(buildSidebar(), BorderLayout.WEST);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(PAGE);
        content.setBorder(BorderFactory.createEmptyBorder(25, 34, 24, 34));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        pageTitle = label("Dashboard", 25, TEXT, Font.BOLD);
        pageSubtitle = label("Overview  /  " + currentUser.getRole().toLowerCase(), 12, MUTED, Font.PLAIN);
        titles.add(pageTitle);
        titles.add(Box.createVerticalStrut(5));
        titles.add(pageSubtitle);
        header.add(titles, BorderLayout.WEST);
        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 9, 0));
        headerActions.setOpaque(false);
        globalSearchField = new HintTextField(20, "Search feedback…");
        styleTextField(globalSearchField);
        globalSearchField.setPreferredSize(new Dimension(220, 38));
        globalSearchField.setMaximumSize(new Dimension(220, 38));
        globalSearchField.setToolTipText("Search feedback by customer, product, or comment");
        globalSearchField.addActionListener(event -> {
            if (feedbackSearch != null) {
                feedbackSearch.setText(globalSearchField.getText());
            }
            showPage("search");
        });
        JButton searchButton = iconButton("search", "Search feedback");
        searchButton.addActionListener(event -> globalSearchField.postActionEvent());
        refreshButton = iconButton("refresh", "Refresh current page");
        refreshButton.addActionListener(event -> refreshCurrentPage());
        JButton profile = new JButton(currentUser.getName(), new UiIcon("user", 16, MUTED));
        profile.setFont(new Font("Segoe UI", Font.BOLD, 12));
        profile.setForeground(TEXT);
        profile.setBackground(PAPER);
        profile.setFocusPainted(true);
        profile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        profile.addActionListener(event -> showPage("settings"));
        headerActions.add(globalSearchField);
        headerActions.add(searchButton);
        headerActions.add(refreshButton);
        headerActions.add(profile);
        header.add(headerActions, BorderLayout.EAST);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                BorderFactory.createEmptyBorder(0, 0, 17, 0)));
        statusLabel = label("Ready", 11, MUTED, Font.PLAIN);
        databaseStateLabel = label("● Database connected", 11, new Color(45, 139, 92), Font.BOLD);
        JPanel topHeader = new JPanel(new BorderLayout(0, 10));
        topHeader.setOpaque(false);
        topHeader.add(header, BorderLayout.NORTH);
        JPanel liveStatus = new JPanel(new BorderLayout());
        liveStatus.setOpaque(false);
        liveStatus.add(databaseStateLabel, BorderLayout.WEST);
        liveStatus.add(statusLabel, BorderLayout.EAST);
        topHeader.add(liveStatus, BorderLayout.SOUTH);
        content.add(topHeader, BorderLayout.NORTH);

        pageLayout = new CardLayout();
        pageCards = new JPanel(pageLayout);
        pageCards.setOpaque(false);
        pageCards.setBorder(BorderFactory.createEmptyBorder(24, 0, 0, 0));
        pageCards.add(buildOverviewPage(), "dashboard");
        if (currentUser instanceof Admin) {
            pageCards.add(buildFeedbackPage(true), "feedback");
        } else {
            pageCards.add(buildProductsPage(), "products");
            pageCards.add(buildSubmitPage(), "submit");
            pageCards.add(buildFeedbackPage(false), "feedback");
        }
        pageCards.add(buildAnalyticsPage(), "analytics");
        pageCards.add(buildSettingsPage(), "settings");
        content.add(pageCards, BorderLayout.CENTER);
        root.add(content, BorderLayout.CENTER);
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("control F"), "search-feedback");
        root.getActionMap().put("search-feedback", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                globalSearchField.requestFocusInWindow();
                globalSearchField.selectAll();
            }
        });
        return root;
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(228, 0));
        sidebar.setBackground(NAVY);
        sidebar.setBorder(BorderFactory.createEmptyBorder(27, 18, 22, 18));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JLabel logo = label("Customer", 20, Color.WHITE, Font.BOLD);
        JLabel logoSub = label("FEEDBACK MANAGEMENT", 9, new Color(155, 174, 255), Font.BOLD);
        sidebar.add(Box.createHorizontalStrut(1));
        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(2));
        sidebar.add(logoSub);
        sidebar.add(Box.createVerticalStrut(30));

        String role = currentUser instanceof Admin ? "ADMIN WORKSPACE" : "CUSTOMER WORKSPACE";
        JLabel roleLabel = label(role, 9, new Color(163, 176, 208), Font.BOLD);
        sidebar.add(roleLabel);
        sidebar.add(Box.createVerticalStrut(12));
        addNavigation(sidebar, "Dashboard", "dashboard", "dashboard");
        if (currentUser instanceof Admin) {
            addNavigation(sidebar, "All Feedback", "feedback", "feedback");
            addNavigation(sidebar, "Search Feedback", "search", "search");
        } else {
            addNavigation(sidebar, "Browse Products", "products", "products");
            addNavigation(sidebar, "Add Feedback", "submit", "add");
            addNavigation(sidebar, "My Feedback", "feedback", "feedback");
            addNavigation(sidebar, "Search Feedback", "search", "search");
        }
        addNavigation(sidebar, "Analytics & Reports", "analytics", "analytics");
        addNavigation(sidebar, "Settings", "settings", "settings");

        sidebar.add(Box.createVerticalGlue());
        JPanel dbStatus = new JPanel(new BorderLayout(8, 0));
        dbStatus.setOpaque(false);
        dbStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(62, 73, 98)),
                BorderFactory.createEmptyBorder(14, 9, 12, 6)));
        JLabel dbDot = label("●", 10, new Color(86, 203, 153), Font.BOLD);
        JLabel dbText = label("Database connected", 11, new Color(202, 214, 230), Font.PLAIN);
        sidebarDatabaseLabel = dbText;
        dbStatus.add(dbDot, BorderLayout.WEST);
        dbStatus.add(dbText, BorderLayout.CENTER);
        sidebar.add(dbStatus);
        JPanel profile = new JPanel();
        profile.setOpaque(false);
        profile.setLayout(new BoxLayout(profile, BoxLayout.Y_AXIS));
        profile.setBorder(BorderFactory.createEmptyBorder(12, 10, 14, 8));
        profile.add(label(currentUser.getName(), 14, Color.WHITE, Font.BOLD));
        profile.add(Box.createVerticalStrut(4));
        profile.add(label(currentUser.getRole().toLowerCase(), 12,
                new Color(163, 176, 208), Font.PLAIN));
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

    private void addNavigation(JPanel sidebar, String text, String page, String icon) {
        JButton button = navButton(text, page, icon);
        navigationButtons.put(page, button);
        button.addActionListener(event -> showPage(page));
        sidebar.add(button);
        sidebar.add(Box.createVerticalStrut(5));
    }

    private JButton navButton(String text) {
        return navButton(text, null, "feedback");
    }

    private JButton navButton(String text, String page, String icon) {
        JButton button = new JButton(text);
        button.setIcon(new UiIcon(icon, 16, new Color(185, 198, 221)));
        button.setIconTextGap(13);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(new Color(220, 231, 224));
        button.setBackground(NAVY);
        button.setFocusPainted(true);
        button.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 10));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent event) {
                if (!pageIsActive(page)) {
                    button.setBackground(NAVY_LIGHT);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent event) {
                updateNavigationButton(button, page);
            }
        });
        updateNavigationButton(button, page);
        return button;
    }

    private boolean pageIsActive(String page) {
        return page != null && page.equals(activePage);
    }

    private void updateNavigationButton(JButton button, String page) {
        boolean selected = pageIsActive(page);
        button.setBackground(selected ? NAVY_LIGHT : NAVY);
        button.setForeground(selected ? Color.WHITE : new Color(220, 231, 224));
        button.setIcon(new UiIcon(iconForPage(page), 16,
                selected ? new Color(146, 165, 255) : new Color(185, 198, 221)));
        button.setFont(new Font("Segoe UI", selected ? Font.BOLD : Font.PLAIN, 14));
    }

    private JPanel buildOverviewPage() {
        JPanel page = new JPanel(new BorderLayout(0, 18));
        page.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JPanel welcome = new JPanel();
        welcome.setOpaque(false);
        welcome.setLayout(new BoxLayout(welcome, BoxLayout.Y_AXIS));
        welcome.add(label("Welcome back, " + currentUser.getName(), 20, TEXT, Font.BOLD));
        welcome.add(Box.createVerticalStrut(4));
        welcome.add(label(currentUser instanceof Admin
                ? "Here is the latest activity across customer feedback."
                : "Here is a snapshot of your feedback activity.", 12, MUTED, Font.PLAIN));
        top.add(welcome, BorderLayout.WEST);
        JButton addFeedback = primaryButton(currentUser instanceof Admin ? "Review feedback" : "+  Add feedback");
        addFeedback.addActionListener(event ->
                showPage(currentUser instanceof Admin ? "feedback" : "submit"));
        top.add(addFeedback, BorderLayout.EAST);
        page.add(top, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setOpaque(false);
        JPanel metrics = new JPanel(new GridLayout(1, 4, 14, 0));
        metrics.setOpaque(false);
        String[] titles = {"Total Feedback", "Positive Feedback", "Neutral Feedback", "Negative Feedback"};
        Color[] accents = {BLUE, new Color(37, 153, 111), new Color(217, 154, 53), new Color(218, 93, 102)};
        summaryValues = new JLabel[4];
        for (int i = 0; i < titles.length; i++) {
            summaryValues[i] = label("—", 27, TEXT, Font.BOLD);
            metrics.add(summaryCard(titles[i], summaryValues[i], accents[i], i));
        }
        body.add(metrics, BorderLayout.NORTH);

        JPanel recent = cardPanel();
        recent.setLayout(new BorderLayout(0, 13));
        JPanel recentHeader = new JPanel(new BorderLayout());
        recentHeader.setOpaque(false);
        JPanel recentTitle = new JPanel();
        recentTitle.setOpaque(false);
        recentTitle.setLayout(new BoxLayout(recentTitle, BoxLayout.Y_AXIS));
        recentTitle.add(label("Recent feedback", 16, TEXT, Font.BOLD));
        recentTitle.add(Box.createVerticalStrut(4));
        recentTitle.add(label("The newest submissions from your workspace.", 11, MUTED, Font.PLAIN));
        recentHeader.add(recentTitle, BorderLayout.WEST);
        JButton viewAll = textButton("View all  →");
        viewAll.addActionListener(event -> showPage("feedback"));
        recentHeader.add(viewAll, BorderLayout.EAST);
        recent.add(recentHeader, BorderLayout.NORTH);

        recentFeedbackModel = feedbackTableModel();
        recentFeedbackTable = createFeedbackTable(recentFeedbackModel);
        recentFeedbackTable.setPreferredScrollableViewportSize(new Dimension(600, 250));
        recentTableHolder = new JPanel(new BorderLayout());
        recentTableHolder.setBackground(PAPER);
        recentTableHolder.add(emptyState("Loading recent feedback…"), BorderLayout.CENTER);
        recent.add(recentTableHolder, BorderLayout.CENTER);
        if (currentUser instanceof Admin) {
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            actions.setOpaque(false);
            JButton view = secondaryButton("View details");
            JButton respond = secondaryButton("Respond");
            JButton delete = dangerButton("Delete");
            view.addActionListener(event -> showSelectedFeedback(recentFeedbackTable, true, false));
            respond.addActionListener(event -> showSelectedFeedback(recentFeedbackTable, true, true));
            delete.addActionListener(event -> deleteSelectedFeedback(recentFeedbackTable, true));
            actions.add(view);
            actions.add(respond);
            actions.add(delete);
            recent.add(actions, BorderLayout.SOUTH);
        } else {
            recentFeedbackTable.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent event) {
                    if (event.getClickCount() == 2) {
                        showSelectedFeedback(recentFeedbackTable, false, false);
                    }
                }
            });
        }
        body.add(recent, BorderLayout.CENTER);
        page.add(body, BorderLayout.CENTER);
        return page;
    }

    private JPanel summaryCard(String title, JLabel value, Color accent, int icon) {
        JPanel card = cardPanel();
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, accent),
                BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER),
                        BorderFactory.createEmptyBorder(16, 17, 16, 15))));
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(label(title, 11, MUTED, Font.BOLD));
        text.add(Box.createVerticalStrut(10));
        text.add(value);
        text.add(Box.createVerticalStrut(5));
        text.add(label("Based on current records", 10, MUTED, Font.PLAIN));
        card.add(text, BorderLayout.CENTER);
        JPanel iconBubble = new JPanel(new BorderLayout());
        iconBubble.setOpaque(true);
        iconBubble.setBackground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 24));
        iconBubble.setPreferredSize(new Dimension(38, 38));
        iconBubble.add(new JLabel(new UiIcon(summaryIconName(icon), 18, accent)), BorderLayout.CENTER);
        card.add(iconBubble, BorderLayout.EAST);
        return card;
    }

    private String summaryIconName(int index) {
        switch (index) {
            case 0: return "feedback";
            case 1: return "positive";
            case 2: return "neutral";
            default: return "negative";
        }
    }

    private void refreshDashboard() {
        if (summaryValues == null || recentFeedbackModel == null) {
            return;
        }
        runTask("Refreshing dashboard…", () -> {
            DashboardService.Summary summary = dashboardService.getSummary(currentUser);
            List<Feedback> recent = currentUser instanceof Admin
                    ? feedbackManager.listRecentFeedback((Admin) currentUser, 8)
                    : feedbackManager.listRecentCustomerFeedback((Customer) currentUser, 8);
            return new DashboardSnapshot(summary, recent);
        }, snapshot -> {
            summaryValues[0].setText(String.valueOf(snapshot.summary.getTotal()));
            summaryValues[1].setText(String.valueOf(snapshot.summary.getPositive()));
            summaryValues[2].setText(String.valueOf(snapshot.summary.getNeutral()));
            summaryValues[3].setText(String.valueOf(snapshot.summary.getNegative()));
            recentFeedbackModel.setRowCount(0);
            recentFeedbackData = snapshot.feedback;
            for (Feedback feedback : snapshot.feedback) {
                addFeedbackRow(recentFeedbackModel, feedback);
            }
            recentTableHolder.removeAll();
            if (snapshot.feedback.isEmpty()) {
                recentTableHolder.add(emptyState("No feedback has been submitted yet."), BorderLayout.CENTER);
            } else {
                recentTableHolder.add(scrollPane(recentFeedbackTable), BorderLayout.CENTER);
            }
            recentTableHolder.revalidate();
            recentTableHolder.repaint();
            if (snapshot.feedback.isEmpty()) {
                status("No feedback records");
            } else {
                status("Dashboard updated");
            }
        });
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
        JPanel page = new JPanel(new BorderLayout());
        page.setOpaque(false);
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
                new JLabel(value == null ? "" : "<html>" + value + " / 5&nbsp;&nbsp;"
                        + starMarkup(value) + "</html>"));
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
        JPanel formActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0));
        formActions.setOpaque(false);
        JButton submit = primaryButton("Save feedback");
        JButton cancel = secondaryButton("Cancel");
        formActions.add(submit);
        formActions.add(cancel);
        formActions.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(formActions);
        cancel.addActionListener(event -> {
            feedbackComment.setText("");
            if (submitProduct.getItemCount() > 0) {
                submitProduct.setSelectedIndex(0);
            }
            showPage("dashboard");
        });
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
                refreshDashboard();
                if (displayedFeedback != null) {
                    loadFeedback(currentUser instanceof Admin);
                }
            });
        });
        page.add(form, BorderLayout.NORTH);
        return page;
    }

    private JPanel buildFeedbackPage(boolean admin) {
        JPanel page = new JPanel(new BorderLayout(0, 14));
        page.setOpaque(false);
        JPanel filters = cardPanel();
        filters.setLayout(new BorderLayout(10, 10));
        JPanel controls = new JPanel(new GridLayout(2, 4, 9, 8));
        controls.setOpaque(false);
        feedbackSearch = new HintTextField(20, "Search customer, product, or comment");
        feedbackSearch.setToolTipText("Search comments, products, and customer names");
        styleTextField(feedbackSearch);
        filterProduct = productCombo(true);
        filterRating = new JComboBox<>(new String[]{"Any rating", "5 stars", "4 stars", "3 stars", "2 stars", "1 star"});
        styleComboBox(filterRating);
        filterSentiment = new JComboBox<>(new String[]{"Any sentiment", "Positive", "Neutral", "Negative"});
        styleComboBox(filterSentiment);
        fromDateField = new HintTextField(14, "From date (yyyy-MM-dd)");
        toDateField = new HintTextField(14, "To date (yyyy-MM-dd)");
        styleTextField(fromDateField);
        styleTextField(toDateField);
        fromDateField.setToolTipText("Start date, format yyyy-MM-dd");
        toDateField.setToolTipText("End date, format yyyy-MM-dd");
        controls.add(feedbackSearch);
        controls.add(filterProduct);
        controls.add(filterRating);
        controls.add(filterSentiment);
        controls.add(fromDateField);
        controls.add(toDateField);
        JPanel filterActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterActions.setOpaque(false);
        JButton search = primaryButton("Apply filters");
        JButton clear = textButton("Clear");
        filterActions.add(search);
        filterActions.add(clear);
        controls.add(filterActions);
        filters.add(controls, BorderLayout.CENTER);
        page.add(filters, BorderLayout.NORTH);

        JPanel results = new JPanel(new BorderLayout(0, 10));
        results.setOpaque(false);
        feedbackTableModel = feedbackTableModel();
        feedbackTable = createFeedbackTable(feedbackTableModel);
        JPanel tablePanel = cardPanel();
        tablePanel.setLayout(new BorderLayout());
        feedbackTableHolder = new JPanel(new BorderLayout());
        feedbackTableHolder.setBackground(PAPER);
        feedbackTableHolder.add(emptyState("Loading feedback…"), BorderLayout.CENTER);
        tablePanel.add(feedbackTableHolder, BorderLayout.CENTER);
        results.add(tablePanel, BorderLayout.CENTER);
        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);
        JPanel pagination = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pagination.setOpaque(false);
        previousFeedbackPage = secondaryButton("Previous");
        nextFeedbackPage = secondaryButton("Next");
        feedbackPageLabel = label("Loading…", 11, MUTED, Font.PLAIN);
        previousFeedbackPage.setEnabled(false);
        nextFeedbackPage.setEnabled(false);
        previousFeedbackPage.addActionListener(event -> {
            if (feedbackCurrentPage > 0) {
                feedbackCurrentPage--;
                renderFeedbackPage();
            }
        });
        nextFeedbackPage.addActionListener(event -> {
            if ((feedbackCurrentPage + 1) * FEEDBACK_PAGE_SIZE < displayedFeedback.size()) {
                feedbackCurrentPage++;
                renderFeedbackPage();
            }
        });
        pagination.add(previousFeedbackPage);
        pagination.add(feedbackPageLabel);
        pagination.add(nextFeedbackPage);
        actions.add(pagination, BorderLayout.WEST);
        JPanel recordActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        recordActions.setOpaque(false);
        JButton view = secondaryButton("View details");
        JButton export = secondaryButton("Export CSV");
        view.addActionListener(event -> showSelectedFeedback(feedbackTable, false, false));
        export.addActionListener(event -> exportFeedbackCsv());
        recordActions.add(export);
        recordActions.add(view);
        if (admin) {
            JButton respond = secondaryButton("Respond");
            JButton delete = dangerButton("Delete");
            respond.addActionListener(event -> showSelectedFeedback(feedbackTable, false, true));
            delete.addActionListener(event -> deleteSelectedFeedback(feedbackTable, false));
            recordActions.add(respond);
            recordActions.add(delete);
        }
        actions.add(recordActions, BorderLayout.EAST);
        results.add(actions, BorderLayout.SOUTH);
        page.add(results, BorderLayout.CENTER);
        search.addActionListener(event -> loadFeedback(admin));
        feedbackSearch.addActionListener(event -> loadFeedback(admin));
        fromDateField.addActionListener(event -> loadFeedback(admin));
        toDateField.addActionListener(event -> loadFeedback(admin));
        clear.addActionListener(event -> {
            feedbackSearch.setText("");
            filterProduct.setSelectedIndex(0);
            filterRating.setSelectedIndex(0);
            filterSentiment.setSelectedIndex(0);
            fromDateField.setText("");
            toDateField.setText("");
            loadFeedback(admin);
        });
        feedbackTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() == 2) {
                    showSelectedFeedback(feedbackTable, false, false);
                }
            }
        });
        loadFeedback(admin);
        return page;
    }

    private DefaultTableModel feedbackTableModel() {
        return new DefaultTableModel(
                new Object[]{"ID", "Customer", "Product", "Rating", "Sentiment", "Submitted", "Response"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JTable createFeedbackTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setForeground(TEXT);
        table.setBackground(PAPER);
        table.setSelectionBackground(ACCENT_LIGHT);
        table.setSelectionForeground(TEXT);
        table.setRowHeight(38);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setForeground(MUTED);
        table.getTableHeader().setBackground(PAPER);
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        table.getColumnModel().getColumn(0).setPreferredWidth(48);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(155);
        table.getColumnModel().getColumn(3).setPreferredWidth(64);
        table.getColumnModel().getColumn(4).setPreferredWidth(95);
        table.getColumnModel().getColumn(5).setPreferredWidth(145);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);
        return table;
    }

    private void addFeedbackRow(DefaultTableModel model, Feedback feedback) {
        model.addRow(new Object[]{feedback.getId(), feedback.getCustomerName(), feedback.getProductName(),
                feedback.getRating() + " / 5", sentiment(feedback.getRating()),
                formatDate(feedback.getCreatedAt()), feedback.getResponse() == null ? "Awaiting" : "Responded"});
    }

    private String sentiment(int rating) {
        if (rating >= 4) {
            return "Positive";
        }
        if (rating == 3) {
            return "Neutral";
        }
        return "Negative";
    }

    private Feedback selectedFeedback(JTable table, boolean recent) {
        int selected = table.getSelectedRow();
        List<Feedback> source = recent ? recentFeedbackData : displayedFeedback;
        if (selected < 0 || source == null) {
            showError("Select a feedback record first.");
            return null;
        }
        int modelRow = table.convertRowIndexToModel(selected);
        long id = ((Number) table.getModel().getValueAt(modelRow, 0)).longValue();
        for (Feedback feedback : source) {
            if (feedback.getId() == id) {
                return feedback;
            }
        }
        showError("The selected feedback is no longer available. Refresh the list and try again.");
        return null;
    }

    private void showSelectedFeedback(JTable table, boolean recent, boolean respond) {
        Feedback feedback = selectedFeedback(table, recent);
        if (feedback == null) {
            return;
        }
        if (respond) {
            promptResponse(feedback);
            return;
        }
        String details = "<html><div style='width:420px'>"
                + "<b>" + escapeHtml(feedback.getProductName()) + "</b><br>"
                + escapeHtml(feedback.getCustomerName()) + " &nbsp;·&nbsp; "
                + feedback.getRating() + "/5 &nbsp;·&nbsp; " + formatDate(feedback.getCreatedAt())
                + "<br><br>" + escapeHtml(feedback.getComment()).replace("\n", "<br>")
                + (feedback.getResponse() == null ? "" : "<br><br><b>Team response</b><br>"
                + escapeHtml(feedback.getResponse().getText()).replace("\n", "<br>"))
                + "</div></html>";
        JOptionPane.showMessageDialog(this, details, "Feedback #" + feedback.getId(),
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteSelectedFeedback(JTable table, boolean recent) {
        if (!(currentUser instanceof Admin)) {
            return;
        }
        Feedback feedback = selectedFeedback(table, recent);
        if (feedback == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete feedback #" + feedback.getId() + " and its response? This cannot be undone.",
                "Delete feedback", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            runTask("Deleting feedback…", () -> {
                feedbackManager.deleteFeedback((Admin) currentUser, feedback.getId());
                return null;
            }, ignored -> {
                loadFeedback(true);
                refreshDashboard();
            });
        }
    }

    private void exportFeedbackCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export feedback");
        chooser.setSelectedFile(new java.io.File("feedback-report.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path destination = chooser.getSelectedFile().toPath();
        runTask("Exporting feedback…", () -> {
            List<Feedback> rows = currentUser instanceof Admin
                    ? feedbackManager.listAllFeedback((Admin) currentUser, "", null, null)
                    : feedbackManager.listCustomerFeedback((Customer) currentUser, "", null, null);
            try (BufferedWriter writer = Files.newBufferedWriter(destination, StandardCharsets.UTF_8)) {
                writer.write("Feedback ID,Customer,Product,Rating,Sentiment,Submitted,Comment,Response");
                writer.newLine();
                for (Feedback feedback : rows) {
                    writer.write(csv(feedback.getId()) + "," + csv(feedback.getCustomerName()) + ","
                            + csv(feedback.getProductName()) + "," + csv(feedback.getRating()) + ","
                            + csv(sentiment(feedback.getRating())) + "," + csv(formatDate(feedback.getCreatedAt()))
                            + "," + csv(feedback.getComment()) + ","
                            + csv(feedback.getResponse() == null ? "" : feedback.getResponse().getText()));
                    writer.newLine();
                }
            }
            return rows.size();
        }, count -> JOptionPane.showMessageDialog(this,
                "Exported " + count + " feedback record" + (count == 1 ? "" : "s") + ".",
                "Export complete", JOptionPane.INFORMATION_MESSAGE));
    }

    private String csv(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    private JPanel buildAnalyticsPage() {
        JPanel page = new JPanel(new BorderLayout(14, 0));
        page.setOpaque(false);
        JPanel left = new JPanel(new BorderLayout(0, 14));
        left.setOpaque(false);
        JPanel intro = cardPanel();
        intro.setLayout(new BorderLayout());
        JPanel introText = new JPanel();
        introText.setOpaque(false);
        introText.setLayout(new BoxLayout(introText, BoxLayout.Y_AXIS));
        introText.add(label("Feedback analytics", 17, TEXT, Font.BOLD));
        introText.add(Box.createVerticalStrut(5));
        introText.add(label("Rating and sentiment distribution from real feedback records.",
                12, MUTED, Font.PLAIN));
        intro.add(introText, BorderLayout.CENTER);
        JButton export = secondaryButton("Export report");
        export.addActionListener(event -> exportFeedbackCsv());
        intro.add(export, BorderLayout.EAST);
        left.add(intro, BorderLayout.NORTH);

        JPanel analyticsCharts = new JPanel(new GridLayout(2, 1, 0, 14));
        analyticsCharts.setOpaque(false);
        JPanel ratingCard = cardPanel();
        ratingCard.setLayout(new BorderLayout(0, 12));
        ratingCard.add(label("Rating breakdown", 14, TEXT, Font.BOLD), BorderLayout.NORTH);
        analyticsRatingsPanel = new JPanel();
        analyticsRatingsPanel.setOpaque(false);
        analyticsRatingsPanel.setLayout(new BoxLayout(analyticsRatingsPanel, BoxLayout.Y_AXIS));
        analyticsRatingsPanel.add(emptyState("Loading analytics…"));
        ratingCard.add(analyticsRatingsPanel, BorderLayout.CENTER);
        analyticsCharts.add(ratingCard);

        JPanel trendCard = cardPanel();
        trendCard.setLayout(new BorderLayout(0, 10));
        trendCard.add(label("Feedback activity · last 7 days", 14, TEXT, Font.BOLD),
                BorderLayout.NORTH);
        analyticsTrendPanel = new JPanel();
        analyticsTrendPanel.setOpaque(false);
        analyticsTrendPanel.setLayout(new BoxLayout(analyticsTrendPanel, BoxLayout.Y_AXIS));
        analyticsTrendPanel.add(label("Loading activity…", 12, MUTED, Font.PLAIN));
        trendCard.add(analyticsTrendPanel, BorderLayout.CENTER);
        analyticsCharts.add(trendCard);
        left.add(analyticsCharts, BorderLayout.CENTER);
        page.add(left, BorderLayout.CENTER);

        if (currentUser instanceof Admin) {
            JPanel productCard = cardPanel();
            productCard.setPreferredSize(new Dimension(380, 0));
            productCard.setLayout(new BorderLayout(0, 12));
            productCard.add(label("Product performance", 14, TEXT, Font.BOLD), BorderLayout.NORTH);
            analyticsProductModel = new DefaultTableModel(
                    new Object[]{"Product", "Feedback", "Avg. rating"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            JTable productTable = createCompactTable(analyticsProductModel);
            productCard.add(scrollPane(productTable), BorderLayout.CENTER);
            page.add(productCard, BorderLayout.EAST);
        } else {
            JPanel note = cardPanel();
            note.setPreferredSize(new Dimension(280, 0));
            note.setLayout(new BoxLayout(note, BoxLayout.Y_AXIS));
            note.add(label("About these insights", 14, TEXT, Font.BOLD));
            note.add(Box.createVerticalStrut(11));
            note.add(label("<html>Ratings are grouped as positive (4–5), neutral (3), "
                    + "and negative (1–2). These figures only include feedback on your account.</html>",
                    12, MUTED, Font.PLAIN));
            page.add(note, BorderLayout.EAST);
        }
        return page;
    }

    private JTable createCompactTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setForeground(TEXT);
        table.setBackground(PAPER);
        table.setRowHeight(38);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER);
        table.setSelectionBackground(ACCENT_LIGHT);
        table.setAutoCreateRowSorter(true);
        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setForeground(MUTED);
        table.getTableHeader().setBackground(PAPER);
        return table;
    }

    private void refreshAnalytics() {
        runTask("Loading analytics…", () -> {
            DashboardService.Summary summary = dashboardService.getSummary(currentUser);
            List<DashboardService.RatingCount> ratings = dashboardService.getRatingBreakdown(currentUser);
            List<DashboardService.DailyCount> trend = dashboardService.getDailyTrend(currentUser);
            List<ProductService.ProductStats> productStats = currentUser instanceof Admin
                    ? productService.getStatistics((Admin) currentUser)
                    : java.util.Collections.<ProductService.ProductStats>emptyList();
            return new AnalyticsSnapshot(summary, ratings, trend, productStats);
        }, snapshot -> {
            analyticsRatingsPanel.removeAll();
            long max = 1;
            for (DashboardService.RatingCount item : snapshot.ratings) {
                max = Math.max(max, item.getCount());
            }
            Color[] ratingColors = {
                    new Color(219, 91, 104), new Color(228, 137, 76),
                    new Color(218, 169, 68), new Color(76, 153, 177),
                    new Color(61, 160, 119)
            };
            for (DashboardService.RatingCount item : snapshot.ratings) {
                JPanel row = new JPanel(new BorderLayout(12, 0));
                row.setOpaque(false);
                row.setBorder(BorderFactory.createEmptyBorder(7, 0, 7, 0));
                JLabel rating = label(item.getRating() + " star" + (item.getRating() == 1 ? "" : "s"),
                        12, TEXT, Font.BOLD);
                JProgressBar bar = new JProgressBar(0, 100);
                bar.setValue((int) Math.round(item.getCount() * 100.0 / max));
                bar.setString(String.valueOf(item.getCount()));
                bar.setStringPainted(true);
                bar.setForeground(ratingColors[item.getRating() - 1]);
                bar.setBackground(new Color(237, 240, 246));
                bar.setBorderPainted(false);
                bar.setPreferredSize(new Dimension(180, 22));
                row.add(rating, BorderLayout.WEST);
                row.add(bar, BorderLayout.CENTER);
                analyticsRatingsPanel.add(row);
            }
            JLabel sentimentSummary = label("Positive  " + snapshot.summary.getPositive()
                    + "     Neutral  " + snapshot.summary.getNeutral()
                    + "     Negative  " + snapshot.summary.getNegative(),
                    12, MUTED, Font.PLAIN);
            sentimentSummary.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));
            analyticsRatingsPanel.add(sentimentSummary);
            analyticsRatingsPanel.revalidate();
            analyticsRatingsPanel.repaint();

            analyticsTrendPanel.removeAll();
            long maxDaily = 1;
            for (DashboardService.DailyCount day : snapshot.trend) {
                maxDaily = Math.max(maxDaily, day.getCount());
            }
            for (DashboardService.DailyCount day : snapshot.trend) {
                JPanel row = new JPanel(new BorderLayout(12, 0));
                row.setOpaque(false);
                row.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
                row.add(label(DateTimeFormatter.ofPattern("EEE, MMM d").format(day.getDate()),
                        11, MUTED, Font.PLAIN), BorderLayout.WEST);
                JProgressBar bar = new JProgressBar(0, 100);
                bar.setValue((int) Math.round(day.getCount() * 100.0 / maxDaily));
                bar.setString(String.valueOf(day.getCount()));
                bar.setStringPainted(true);
                bar.setForeground(CYAN);
                bar.setBackground(new Color(237, 240, 246));
                bar.setBorderPainted(false);
                bar.setPreferredSize(new Dimension(150, 18));
                row.add(bar, BorderLayout.CENTER);
                analyticsTrendPanel.add(row);
            }
            analyticsTrendPanel.revalidate();
            analyticsTrendPanel.repaint();

            if (analyticsProductModel != null) {
                analyticsProductModel.setRowCount(0);
                for (ProductService.ProductStats stat : snapshot.products) {
                    analyticsProductModel.addRow(new Object[]{stat.getProductName(), stat.getFeedbackCount(),
                            String.format("%.2f / 5", stat.getAverageRating())});
                }
            }
            status("Analytics updated");
        });
    }

    private JPanel buildSettingsPage() {
        JPanel page = verticalPanel();
        JPanel account = cardPanel();
        account.setLayout(new BoxLayout(account, BoxLayout.Y_AXIS));
        account.add(label("Account profile", 16, TEXT, Font.BOLD));
        account.add(Box.createVerticalStrut(14));
        account.add(settingRow("Name", currentUser.getName()));
        account.add(settingRow("Email", currentUser.getEmail()));
        account.add(settingRow("Role", currentUser.getRole()));
        page.add(account);
        page.add(Box.createVerticalStrut(14));

        JPanel connection = cardPanel();
        connection.setLayout(new BoxLayout(connection, BoxLayout.Y_AXIS));
        connection.add(label("Database connection", 16, TEXT, Font.BOLD));
        connection.add(Box.createVerticalStrut(7));
        connection.add(label("Connection settings are read from db.properties or DB_URL / DB_USER / DB_PASSWORD.",
                12, MUTED, Font.PLAIN));
        connection.add(Box.createVerticalStrut(13));
        JPanel state = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        state.setOpaque(false);
        state.add(label("●  ", 12, new Color(45, 139, 92), Font.BOLD));
        settingsConnectionLabel = label("Connected", 12, new Color(45, 139, 92), Font.BOLD);
        state.add(settingsConnectionLabel);
        connection.add(state);
        connection.add(Box.createVerticalStrut(11));
        JButton testConnection = secondaryButton("Test connection");
        testConnection.setAlignmentX(Component.LEFT_ALIGNMENT);
        testConnection.addActionListener(event -> testDatabaseConnection());
        connection.add(testConnection);
        page.add(connection);
        return page;
    }

    private JPanel settingRow(String name, String value) {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(7, 0, 7, 0));
        row.add(label(name, 12, MUTED, Font.PLAIN), BorderLayout.WEST);
        row.add(label(value, 12, TEXT, Font.BOLD), BorderLayout.CENTER);
        return row;
    }

    private void testDatabaseConnection() {
        runTask("Testing database connection…", () -> {
            try (java.sql.Connection connection = DBConnection.getConnection()) {
                return connection.isValid(3);
            }
        }, connected -> {
            updateDatabaseStatus(Boolean.TRUE.equals(connected));
            status(Boolean.TRUE.equals(connected) ? "Database connection successful" : "Database unavailable");
        });
    }

    private void refreshCurrentPage() {
        switch (activePage) {
            case "dashboard":
                refreshDashboard();
                break;
            case "feedback":
            case "search":
                if (feedbackSearch != null) {
                    loadFeedback(currentUser instanceof Admin);
                }
                break;
            case "analytics":
                refreshAnalytics();
                break;
            case "settings":
                testDatabaseConnection();
                break;
            default:
                status("Nothing to refresh on this page");
        }
    }

    private void loadFeedback(boolean admin) {
        String search = feedbackSearch.getText();
        ProductOption option = (ProductOption) filterProduct.getSelectedItem();
        Long productId = option == null || option.product == null ? null : option.product.getId();
        int ratingIndex = filterRating.getSelectedIndex();
        Integer rating = ratingIndex <= 0 ? null : 6 - ratingIndex;
        String sentiment = filterSentiment.getSelectedIndex() <= 0 ? null
                : ((String) filterSentiment.getSelectedItem()).toLowerCase(java.util.Locale.ROOT);
        LocalDate fromDate;
        LocalDate toDate;
        try {
            fromDate = parseOptionalDate(fromDateField.getText(), "Start date");
            toDate = parseOptionalDate(toDateField.getText(), "End date");
            if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
                showError("The start date must be on or before the end date.");
                return;
            }
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
            return;
        }
        runTask("Loading feedback…", () -> admin
                        ? feedbackManager.listAllFeedback((Admin) currentUser, search, productId, rating,
                                sentiment, fromDate, toDate)
                        : feedbackManager.listCustomerFeedback((Customer) currentUser, search, productId, rating,
                                sentiment, fromDate, toDate),
                results -> renderFeedback(results, admin));
    }

    private void renderFeedback(List<Feedback> results, boolean admin) {
        displayedFeedback = results;
        feedbackCurrentPage = 0;
        renderFeedbackPage();
        status(results.size() + (results.size() == 1 ? " feedback item" : " feedback items"));
    }

    private void renderFeedbackPage() {
        feedbackTableModel.setRowCount(0);
        int start = feedbackCurrentPage * FEEDBACK_PAGE_SIZE;
        int end = Math.min(start + FEEDBACK_PAGE_SIZE, displayedFeedback.size());
        for (int i = start; i < end; i++) {
            addFeedbackRow(feedbackTableModel, displayedFeedback.get(i));
        }
        feedbackTableModel.fireTableDataChanged();
        feedbackTableHolder.removeAll();
        if (displayedFeedback.isEmpty()) {
            feedbackTableHolder.add(emptyState("No feedback matches these filters."), BorderLayout.CENTER);
        } else {
            feedbackTableHolder.add(scrollPane(feedbackTable), BorderLayout.CENTER);
        }
        feedbackTableHolder.revalidate();
        feedbackTableHolder.repaint();
        feedbackPageLabel.setText(displayedFeedback.isEmpty() ? "0 records"
                : (start + 1) + "–" + end + " of " + displayedFeedback.size());
        previousFeedbackPage.setEnabled(feedbackCurrentPage > 0);
        nextFeedbackPage.setEnabled(end < displayedFeedback.size());
    }

    private LocalDate parseOptionalDate(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(label + " must use yyyy-MM-dd.");
        }
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
        top.add(label("<html>" + starMarkup(feedback.getRating()) + "&nbsp;&nbsp;"
                        + feedback.getRating() + "/5</html>",
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
        comment.setBackground(new Color(250, 247, 241));
        comment.setBorder(BorderFactory.createEmptyBorder(11, 12, 11, 12));
        content.add(comment);
        if (feedback.getResponse() != null) {
            JPanel response = new JPanel();
            response.setLayout(new BoxLayout(response, BoxLayout.Y_AXIS));
            response.setBackground(new Color(240, 243, 255));
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
            }, ignored -> {
                loadFeedback(true);
                refreshDashboard();
            });
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
            }, ignored -> {
                loadFeedback(true);
                refreshDashboard();
            });
        }
    }

    private void showPage(String page) {
        if (pageCards == null) {
            return;
        }
        activePage = page;
        for (Map.Entry<String, JButton> entry : navigationButtons.entrySet()) {
            updateNavigationButton(entry.getValue(), entry.getKey());
        }
        pageLayout.show(pageCards, "search".equals(page) ? "feedback" : page);
        switch (page) {
            case "dashboard":
                setPageHeading("Dashboard", "Overview  /  " + currentUser.getRole().toLowerCase());
                refreshDashboard();
                break;
            case "products":
                setPageHeading("Browse products", "Product catalog  /  Share your experience");
                break;
            case "submit":
                setPageHeading("Add feedback", "Tell us about a product you have used.");
                break;
            case "feedback":
                setPageHeading(currentUser instanceof Admin ? "All feedback" : "My feedback",
                        "Browse, sort, search, and filter feedback records.");
                if (feedbackSearch != null) {
                    loadFeedback(currentUser instanceof Admin);
                }
                break;
            case "search":
                setPageHeading("Search feedback", "Find records by customer, product, rating, or date.");
                if (feedbackSearch != null) {
                    feedbackSearch.setText(globalSearchField.getText());
                    loadFeedback(currentUser instanceof Admin);
                    feedbackSearch.requestFocusInWindow();
                }
                break;
            case "analytics":
                setPageHeading("Analytics & Reports", "Real feedback trends and rating distribution.");
                refreshAnalytics();
                break;
            case "settings":
                setPageHeading("Settings", "Your profile and database connection.");
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
        styleComboBox(combo);
        combo.setPreferredSize(new Dimension(180, 40));
        return combo;
    }

    private void styleComboBox(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setForeground(TEXT);
        combo.setBackground(PAPER);
        combo.setBorder(new RoundedLineBorder(BORDER, 9));
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
                    updateDatabaseStatus(true);
                    success.accept(result);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    status("Operation interrupted");
                    showError("The operation was interrupted. Please try again.");
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    status("Action failed");
                    if (isConnectionFailure(cause)) {
                        updateDatabaseStatus(false);
                    }
                    String message = cause instanceof java.sql.SQLException
                            ? "The database request could not be completed. Check the connection and try again."
                            : cause == null || cause.getMessage() == null
                                    ? "The operation could not be completed." : cause.getMessage();
                    showError(message);
                }
            }
        }.execute();
    }

    private void status(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    private void updateDatabaseStatus(boolean connected) {
        Color color = connected ? new Color(45, 139, 92) : new Color(200, 75, 75);
        if (databaseStateLabel != null) {
            databaseStateLabel.setText(connected ? "● Database connected" : "● Database unavailable");
            databaseStateLabel.setForeground(color);
        }
        if (settingsConnectionLabel != null) {
            settingsConnectionLabel.setText(connected ? "Connected" : "Not connected");
            settingsConnectionLabel.setForeground(color);
        }
        if (sidebarDatabaseLabel != null) {
            sidebarDatabaseLabel.setText(connected ? "Database connected" : "Database unavailable");
            sidebarDatabaseLabel.setForeground(connected
                    ? new Color(202, 214, 230) : new Color(255, 165, 165));
        }
    }

    private boolean isConnectionFailure(Throwable cause) {
        if (!(cause instanceof java.sql.SQLException)) {
            return false;
        }
        String state = ((java.sql.SQLException) cause).getSQLState();
        return state != null && state.startsWith("08");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Something went wrong", JOptionPane.ERROR_MESSAGE);
    }

    private JPanel fieldBlock(String title, Component field) {
        JPanel block = new JPanel();
        block.setBackground(PAPER);
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        JLabel label = label(title, 12, TEXT, Font.BOLD);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (field instanceof JTextField) {
            styleTextField((JTextField) field);
        } else if (field instanceof JComboBox) {
            field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
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

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(TEXT);
        field.setBackground(PAPER);
        field.setPreferredSize(new Dimension(320, 42));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(BORDER, 10),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
    }

    private JPanel formPanel() {
        JPanel form = new JPanel();
        form.setBackground(PAPER);
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
        JPanel panel = new RoundedPanel(PAPER, BORDER, 14);
        panel.setBorder(BorderFactory.createEmptyBorder(19, 21, 19, 21));
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
        JButton button = new RoundedButton(text, 12);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(PAPER);
        button.setBackground(BLUE);
        button.setFocusPainted(true);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 17, 10, 17));
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent event) {
                button.setBackground(new Color(43, 94, 72));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent event) {
                button.setBackground(BLUE);
            }
        });
        return button;
    }

    private JButton secondaryButton(String text) {
        JButton button = new RoundedButton(text, 12);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(BLUE);
        button.setBackground(ACCENT_LIGHT);
        button.setFocusPainted(true);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));
        return button;
    }

    private JButton textButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setForeground(BLUE);
        button.setBackground(PAPER);
        button.setFocusPainted(true);
        button.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        return button;
    }

    private JButton iconButton(String icon, String tooltip) {
        JButton button = new JButton(new UiIcon(icon, 17, MUTED));
        button.setToolTipText(tooltip);
        button.getAccessibleContext().setAccessibleName(tooltip);
        button.setBackground(PAPER);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(9, 10, 9, 10)));
        button.setFocusPainted(true);
        return button;
    }

    private JButton dangerButton(String text) {
        JButton button = new RoundedButton(text, 12);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(new Color(151, 56, 45));
        button.setBackground(new Color(250, 236, 231));
        button.setFocusPainted(true);
        button.setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));
        return button;
    }

    private JLabel label(String text, int size, Color color, int style) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", style, size));
        label.setForeground(color);
        return label;
    }

    private JLabel editorialLabel(String text, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Georgia", Font.PLAIN, size));
        label.setForeground(color);
        return label;
    }

    private String iconForPage(String page) {
        if ("dashboard".equals(page)) return "dashboard";
        if ("products".equals(page)) return "products";
        if ("submit".equals(page)) return "add";
        if ("search".equals(page)) return "search";
        if ("analytics".equals(page)) return "analytics";
        if ("settings".equals(page)) return "settings";
        return "feedback";
    }

    private void styleTextArea(JTextArea area) {
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setForeground(TEXT);
        area.setBackground(PAPER);
        area.setMargin(new Insets(8, 10, 8, 10));
        area.setBorder(new RoundedLineBorder(BORDER, 10));
    }

    private String formatDate(LocalDateTime date) {
        return date == null ? "Date unavailable" : DATE_FORMAT.format(date);
    }

    private String starMarkup(int rating) {
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            value.append("<span style='color:")
                    .append(i < rating ? "#E5A526" : "#C8D0DC")
                    .append("'>")
                    .append(i < rating ? "★" : "☆")
                    .append("</span>");
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

    private static final class DashboardSnapshot {
        private final DashboardService.Summary summary;
        private final List<Feedback> feedback;

        private DashboardSnapshot(DashboardService.Summary summary, List<Feedback> feedback) {
            this.summary = summary;
            this.feedback = feedback;
        }
    }

    private static final class HintTextField extends JTextField {
        private final String hint;

        private HintTextField(int columns, String hint) {
            super(columns);
            this.hint = hint;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (getText().isEmpty() && !hasFocus()) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setFont(getFont());
                g.setColor(MUTED);
                Insets insets = getInsets();
                int y = (getHeight() - g.getFontMetrics().getHeight()) / 2 + g.getFontMetrics().getAscent();
                g.drawString(hint, insets.left + 2, y);
                g.dispose();
            }
        }
    }

    private static final class AnalyticsSnapshot {
        private final DashboardService.Summary summary;
        private final List<DashboardService.RatingCount> ratings;
        private final List<DashboardService.DailyCount> trend;
        private final List<ProductService.ProductStats> products;

        private AnalyticsSnapshot(DashboardService.Summary summary,
                                  List<DashboardService.RatingCount> ratings,
                                  List<DashboardService.DailyCount> trend,
                                  List<ProductService.ProductStats> products) {
            this.summary = summary;
            this.ratings = ratings;
            this.trend = trend;
            this.products = products;
        }
    }

    private static final class UiIcon implements Icon {
        private final String name;
        private final int size;
        private final Color color;

        private UiIcon(String name, int size, Color color) {
            this.name = name;
            this.size = size;
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.translate(x, y);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(color);
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int s = size;
            switch (name) {
                case "dashboard":
                    g.drawRoundRect(2, 2, s / 2 - 2, s / 2 - 2, 2, 2);
                    g.drawRoundRect(s / 2 + 1, 2, s / 2 - 3, s / 2 - 2, 2, 2);
                    g.drawRoundRect(2, s / 2 + 1, s / 2 - 2, s / 2 - 3, 2, 2);
                    g.drawRoundRect(s / 2 + 1, s / 2 + 1, s / 2 - 3, s / 2 - 3, 2, 2);
                    break;
                case "search":
                    g.drawOval(2, 2, s - 7, s - 7);
                    g.drawLine(s - 6, s - 6, s - 2, s - 2);
                    break;
                case "refresh":
                    g.drawArc(3, 3, s - 6, s - 6, 35, 285);
                    g.drawLine(s - 4, 3, s - 2, 7);
                    g.drawLine(s - 4, 3, s - 8, 4);
                    break;
                case "add":
                    g.drawOval(2, 2, s - 4, s - 4);
                    g.drawLine(s / 2, 5, s / 2, s - 5);
                    g.drawLine(5, s / 2, s - 5, s / 2);
                    break;
                case "products":
                    g.drawRoundRect(2, 3, s - 4, s - 6, 3, 3);
                    g.drawLine(2, 7, s - 2, 7);
                    g.drawLine(s / 2, 3, s / 2, s - 3);
                    break;
                case "analytics":
                    g.drawLine(2, s - 2, s - 2, s - 2);
                    g.drawLine(3, s - 3, 3, s / 2 + 1);
                    g.drawLine(s / 2 - 1, s - 3, s / 2 - 1, 3);
                    g.drawLine(s - 3, s - 3, s - 3, s / 2 - 1);
                    break;
                case "settings":
                    g.drawOval(s / 2 - 3, s / 2 - 3, 6, 6);
                    for (int angle = 0; angle < 360; angle += 45) {
                        double r1 = s * 0.27;
                        double r2 = s * 0.42;
                        double radians = Math.toRadians(angle);
                        int x1 = s / 2 + (int) (Math.cos(radians) * r1);
                        int y1 = s / 2 + (int) (Math.sin(radians) * r1);
                        int x2 = s / 2 + (int) (Math.cos(radians) * r2);
                        int y2 = s / 2 + (int) (Math.sin(radians) * r2);
                        g.drawLine(x1, y1, x2, y2);
                    }
                    break;
                case "user":
                    g.drawOval(s / 2 - 3, 2, 6, 6);
                    g.drawArc(2, s / 2 - 1, s - 4, s / 2 + 1, 0, 180);
                    break;
                case "positive":
                    g.drawOval(2, 2, s - 4, s - 4);
                    g.drawArc(5, 4, s - 10, s - 8, 205, 130);
                    break;
                case "neutral":
                    g.drawOval(2, 2, s - 4, s - 4);
                    g.drawLine(5, s / 2 + 2, s - 5, s / 2 + 2);
                    break;
                case "negative":
                    g.drawOval(2, 2, s - 4, s - 4);
                    g.drawArc(5, s / 2 - 2, s - 10, s / 2, 25, 130);
                    break;
                default:
                    g.drawRoundRect(2, 3, s - 4, s - 6, 4, 4);
                    g.drawLine(5, s / 2, s - 5, s / 2);
                    g.drawLine(5, s / 2 + 3, s - 5, s / 2 + 3);
                    break;
            }
            g.dispose();
        }
    }

    private static final class RoundedPanel extends JPanel {
        private final Color fill;
        private final Color outline;
        private final int radius;

        private RoundedPanel(Color fill, Color outline, int radius) {
            this.fill = fill;
            this.outline = outline;
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g.setColor(outline);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static final class RoundedButton extends JButton {
        private final int radius;

        private RoundedButton(String text, int radius) {
            super(text);
            this.radius = radius;
            setOpaque(false);
            setContentAreaFilled(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(isEnabled() ? getBackground() : BORDER);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g.dispose();
            super.paintComponent(graphics);
            if (hasFocus()) {
                Graphics2D focusGraphics = (Graphics2D) graphics.create();
                focusGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                focusGraphics.setColor(CYAN);
                focusGraphics.setStroke(new BasicStroke(1.5f));
                focusGraphics.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, radius, radius);
                focusGraphics.dispose();
            }
        }
    }

    private static final class RoundedLineBorder extends AbstractBorder {
        private final Color color;
        private final int radius;

        private RoundedLineBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component component, Graphics graphics, int x, int y, int width, int height) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(component.hasFocus() ? BLUE : color);
            g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g.dispose();
        }

        @Override
        public Insets getBorderInsets(Component component) {
            return new Insets(5, 9, 5, 9);
        }
    }

    private static final class AlternatingRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                       boolean focused, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, focused, row, column);
            if (selected) {
                setBackground(ACCENT_LIGHT);
                setForeground(TEXT);
            } else {
                setBackground(row % 2 == 0 ? PAPER : new Color(248, 249, 252));
                setForeground(TEXT);
            }
            setBorder(BorderFactory.createEmptyBorder(0, 9, 0, 9));
            return this;
        }
    }
}
