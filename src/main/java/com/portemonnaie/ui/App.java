package com.portemonnaie.ui;

import com.portemonnaie.model.Transaction;
import com.portemonnaie.model.User;
import com.portemonnaie.service.AuthService;
import com.portemonnaie.service.WalletService;
import com.portemonnaie.ui.components.NavButton;
import com.portemonnaie.ui.pages.AddTransactionPage;
import com.portemonnaie.ui.pages.CategoriesPage;
import com.portemonnaie.ui.pages.DashboardPage;
import com.portemonnaie.ui.pages.GoalsPage;
import com.portemonnaie.ui.pages.LoginPage;
import com.portemonnaie.ui.pages.SettingsPage;
import com.portemonnaie.ui.pages.StatisticsPage;
import com.portemonnaie.ui.pages.TransactionsPage;
import com.portemonnaie.ui.theme.Theme;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class App extends Application {
    private final AuthService authService = new AuthService();
    private final WalletService service = new WalletService();

    private BorderPane root;
    private VBox sidebar;
    private Label greeting;

    private final List<NavButton> navButtons = new ArrayList<>();
    private NavButton btnDashboard, btnTransactions, btnAdd, btnGoals, btnCategories, btnStats, btnSettings;

    @Override
    public void start(Stage stage) {
        root = new BorderPane();
        root.getStyleClass().add("root-pane");

        Scene scene = new Scene(root, 1080, 720);
        Theme.apply(scene);

        stage.setTitle("Porte-Monnaie");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(620);
        stage.show();

        showLogin();
    }

    public void onAuthenticated(User user) {
        service.setUserName(user.getDisplayName());
        buildMainShell();
    }

    public void showLogin() {
        root.setLeft(null);
        setContent(new LoginPage(this, authService));
    }

    private void buildMainShell() {
        navButtons.clear();
        sidebar = buildSidebar();
        root.setLeft(sidebar);
        showDashboard();
    }

    private VBox buildSidebar() {
        VBox box = new VBox(7);
        box.getStyleClass().add("sidebar");
        box.setPadding(new Insets(24, 16, 22, 16));

        Label appName = new Label("Porte-Monnaie");
        appName.getStyleClass().add("brand");

        greeting = new Label(service.getUserName());
        greeting.getStyleClass().add("brand-sub");

        VBox brandBox = new VBox(2, appName, greeting);
        brandBox.setPadding(new Insets(0, 0, 20, 0));

        btnDashboard = nav("  Tableau de bord", this::showDashboard);
        btnTransactions = nav("  Transactions", this::showTransactions);
        btnAdd = nav("  Ajouter", () -> showAddTransaction(null));
        btnGoals = nav("  Epargne", this::showGoals);
        btnCategories = nav("  Categories", this::showCategories);
        btnStats = nav("  Statistiques", this::showStatistics);
        btnSettings = nav("  Parametres", this::showSettings);

        Region spacer = new Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label footer = new Label("v1.1 - donnees locales securisees");
        footer.getStyleClass().add("sidebar-footer");

        box.getChildren().addAll(brandBox,
                btnDashboard, btnTransactions, btnAdd, btnGoals,
                btnCategories, btnStats, btnSettings, spacer, footer);
        return box;
    }

    private NavButton nav(String text, Runnable action) {
        NavButton b = new NavButton(text);
        b.setOnAction(e -> action.run());
        navButtons.add(b);
        return b;
    }

    private void select(NavButton active) {
        for (NavButton b : navButtons) {
            b.setSelected(b == active);
        }
    }

    public void refreshSidebar() {
        if (greeting != null) greeting.setText(service.getUserName());
    }

    private void setContent(Node node) {
        root.setCenter(node);
    }

    public void showDashboard() {
        setContent(new DashboardPage(this, service));
        select(btnDashboard);
    }

    public void showTransactions() {
        setContent(new TransactionsPage(this, service));
        select(btnTransactions);
    }

    public void showAddTransaction(Transaction editing) {
        setContent(new AddTransactionPage(this, service, editing));
        select(btnAdd);
    }

    public void showGoals() {
        setContent(new GoalsPage(this, service));
        select(btnGoals);
    }

    public void showCategories() {
        setContent(new CategoriesPage(this, service));
        select(btnCategories);
    }

    public void showStatistics() {
        setContent(new StatisticsPage(this, service));
        select(btnStats);
    }

    public void showSettings() {
        setContent(new SettingsPage(this, service));
        select(btnSettings);
    }
}
