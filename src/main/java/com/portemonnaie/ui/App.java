package com.portemonnaie.ui;

import com.portemonnaie.model.Transaction;
import com.portemonnaie.service.WalletService;
import com.portemonnaie.ui.components.NavButton;
import com.portemonnaie.ui.pages.AddTransactionPage;
import com.portemonnaie.ui.pages.CategoriesPage;
import com.portemonnaie.ui.pages.DashboardPage;
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

/**
 * Fenêtre principale de l'application.
 * Contient une barre latérale (navigation) et une zone centrale qui affiche
 * la page sélectionnée. Chaque page est reconstruite à l'affichage pour
 * toujours montrer des données à jour.
 */
public class App extends Application {

    private final WalletService service = new WalletService();

    private BorderPane root;
    private VBox sidebar;
    private Label greeting;

    // Boutons de navigation (pour gérer la surbrillance de la page active)
    private final List<NavButton> navButtons = new ArrayList<>();
    private NavButton btnDashboard, btnTransactions, btnAdd, btnCategories, btnStats, btnSettings;

    @Override
    public void start(Stage stage) {
        root = new BorderPane();
        root.getStyleClass().add("root-pane");

        sidebar = buildSidebar();
        root.setLeft(sidebar);

        Scene scene = new Scene(root, 1080, 720);
        Theme.apply(scene);

        stage.setTitle("Porte-Monnaie");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(620);
        stage.show();

        showDashboard(); // page d'accueil
    }

    // ----------------------------- Barre latérale --------------------------
    private VBox buildSidebar() {
        VBox box = new VBox(6);
        box.getStyleClass().add("sidebar");
        box.setPadding(new Insets(22, 16, 22, 16));

        Label appName = new Label("Porte-Monnaie");
        appName.getStyleClass().add("brand");

        greeting = new Label(service.getUserName());
        greeting.getStyleClass().add("brand-sub");

        VBox brandBox = new VBox(2, appName, greeting);
        brandBox.setPadding(new Insets(0, 0, 18, 0));

        btnDashboard = nav("  Tableau de bord", this::showDashboard);
        btnTransactions = nav("  Transactions", this::showTransactions);
        btnAdd = nav("  Ajouter", () -> showAddTransaction(null));
        btnCategories = nav("  Catégories", this::showCategories);
        btnStats = nav("  Statistiques", this::showStatistics);
        btnSettings = nav("  Paramètres", this::showSettings);

        Region spacer = new Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label footer = new Label("v1.0 — données locales");
        footer.getStyleClass().add("sidebar-footer");

        box.getChildren().addAll(brandBox,
                btnDashboard, btnTransactions, btnAdd,
                btnCategories, btnStats, btnSettings,
                spacer, footer);
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

    /** Met à jour le nom affiché dans la barre latérale (après changement dans les réglages). */
    public void refreshSidebar() {
        greeting.setText(service.getUserName());
    }

    // ----------------------------- Navigation ------------------------------
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

    /** editing == null => ajout ; sinon => modification. */
    public void showAddTransaction(Transaction editing) {
        setContent(new AddTransactionPage(this, service, editing));
        select(btnAdd);
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
