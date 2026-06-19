package com.portemonnaie.ui.pages;

import com.portemonnaie.model.Transaction;
import com.portemonnaie.model.TransactionType;
import com.portemonnaie.service.WalletService;
import com.portemonnaie.ui.App;
import com.portemonnaie.ui.components.StatCard;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

/**
 * Page d'accueil : donne l'impression d'ouvrir un vrai porte-monnaie.
 * Affiche le solde, les totaux, le résumé du mois et les dernières opérations.
 */
public class DashboardPage extends ScrollPane {

    public DashboardPage(App app, WalletService service) {
        getStyleClass().add("page-scroll");
        setFitToWidth(true);

        VBox content = new VBox(20);
        content.setPadding(new Insets(28));

        // --- En-tête (salutation) -------------------------------------------
        Label hello = new Label("Bonjour, " + service.getUserName());
        hello.getStyleClass().add("page-title");
        Label sub = new Label("Voici votre porte-monnaie.");
        sub.getStyleClass().add("page-subtitle");
        VBox header = new VBox(2, hello, sub);

        // --- Carte solde (héros) --------------------------------------------
        VBox balanceCard = new VBox(8);
        balanceCard.getStyleClass().add("hero-card");
        Label balanceTitle = new Label("SOLDE ACTUEL");
        balanceTitle.getStyleClass().add("hero-title");
        double balance = service.getBalance();
        Label balanceValue = new Label(service.formatMoney(balance));
        balanceValue.getStyleClass().add("hero-value");
        balanceValue.getStyleClass().add(balance >= 0 ? "income" : "expense");
        balanceCard.getChildren().addAll(balanceTitle, balanceValue);

        // --- Deux cartes : revenus / dépenses -------------------------------
        StatCard incomeCard = new StatCard("Total des revenus",
                service.formatMoney(service.getTotalIncome()));
        incomeCard.emphasize("income");
        StatCard expenseCard = new StatCard("Total des dépenses",
                service.formatMoney(service.getTotalExpense()));
        expenseCard.emphasize("expense");
        HBox.setHgrow(incomeCard, Priority.ALWAYS);
        HBox.setHgrow(expenseCard, Priority.ALWAYS);
        HBox statsRow = new HBox(20, incomeCard, expenseCard);

        // --- Résumé du mois -------------------------------------------------
        YearMonth now = YearMonth.now();
        double[] month = service.getMonthSummary(now);
        String monthName = now.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH)
                + " " + now.getYear();

        VBox monthCard = new VBox(10);
        monthCard.getStyleClass().add("card");
        Label monthTitle = new Label("Résumé de " + monthName);
        monthTitle.getStyleClass().add("card-title");
        Label monthIncome = new Label("Revenus du mois : " + service.formatMoney(month[0]));
        monthIncome.getStyleClass().add("income");
        Label monthExpense = new Label("Dépenses du mois : " + service.formatMoney(month[1]));
        monthExpense.getStyleClass().add("expense");
        Label monthNet = new Label("Solde du mois : " + service.formatMoney(month[0] - month[1]));
        monthNet.getStyleClass().add("muted");
        monthCard.getChildren().addAll(monthTitle, monthIncome, monthExpense, monthNet);

        // --- Dernières transactions -----------------------------------------
        VBox recentCard = new VBox(8);
        recentCard.getStyleClass().add("card");
        HBox recentHeader = new HBox();
        Label recentTitle = new Label("Dernières transactions");
        recentTitle.getStyleClass().add("card-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button seeAll = new Button("Voir tout");
        seeAll.getStyleClass().add("link-button");
        seeAll.setOnAction(e -> app.showTransactions());
        recentHeader.getChildren().addAll(recentTitle, spacer, seeAll);
        recentHeader.setAlignment(Pos.CENTER_LEFT);
        recentCard.getChildren().add(recentHeader);

        List<Transaction> recent = service.getRecentTransactions(6);
        if (recent.isEmpty()) {
            Label empty = new Label("Aucune transaction pour le moment. "
                    + "Ajoutez-en une depuis le menu « Ajouter ».");
            empty.getStyleClass().add("muted");
            recentCard.getChildren().add(empty);
        } else {
            for (Transaction t : recent) {
                recentCard.getChildren().add(buildRow(t, service));
            }
        }

        content.getChildren().addAll(header, balanceCard, statsRow, monthCard, recentCard);
        setContent(content);
    }

    /** Construit une ligne compacte représentant une transaction. */
    private HBox buildRow(Transaction t, WalletService service) {
        HBox row = new HBox(12);
        row.getStyleClass().add("tx-row");
        row.setAlignment(Pos.CENTER_LEFT);

        VBox left = new VBox(2);
        Label title = new Label(t.getTitle());
        title.getStyleClass().add("tx-title");
        Label meta = new Label(t.getCategory() + "  •  " + t.getDate());
        meta.getStyleClass().add("tx-meta");
        left.getChildren().addAll(title, meta);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        boolean income = t.getType() == TransactionType.REVENU;
        Label amount = new Label((income ? "+" : "-") + service.formatMoney(t.getAmount()));
        amount.getStyleClass().add("tx-amount");
        amount.getStyleClass().add(income ? "income" : "expense");

        row.getChildren().addAll(left, spacer, amount);
        return row;
    }
}
