package com.portemonnaie.ui.pages;

import com.portemonnaie.service.WalletService;
import com.portemonnaie.ui.App;
import com.portemonnaie.ui.components.StatCard;
import javafx.geometry.Insets;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;

/**
 * Page "Statistiques" : graphiques de répartition (dépenses/revenus par
 * catégorie), évolution du solde, mois le plus dépensier et catégorie où
 * l'on dépense le plus.
 */
public class StatisticsPage extends ScrollPane {

    public StatisticsPage(App app, WalletService service) {
        getStyleClass().add("page-scroll");
        setFitToWidth(true);

        VBox content = new VBox(20);
        content.setPadding(new Insets(28));

        Label title = new Label("Statistiques");
        title.getStyleClass().add("page-title");

        // --- Faits marquants ------------------------------------------------
        WalletService.MonthStat topMonth = service.mostExpensiveMonth();
        WalletService.CategoryStat topCat = service.topExpenseCategory();

        StatCard monthCard = new StatCard("Mois le plus dépensier",
                topMonth == null ? "—"
                        : prettyMonth(topMonth.month) + "  ("
                          + service.formatMoney(topMonth.amount) + ")");
        monthCard.emphasize("expense");

        StatCard catCard = new StatCard("Catégorie la plus dépensière",
                topCat == null ? "—"
                        : topCat.category + "  (" + service.formatMoney(topCat.amount) + ")");
        catCard.emphasize("expense");

        HBox.setHgrow(monthCard, Priority.ALWAYS);
        HBox.setHgrow(catCard, Priority.ALWAYS);
        HBox highlights = new HBox(20, monthCard, catCard);

        // --- Camemberts -----------------------------------------------------
        VBox expensePie = pieCard("Dépenses par catégorie",
                service.expensesByCategory());
        VBox incomePie = pieCard("Revenus par catégorie",
                service.incomeByCategory());
        HBox.setHgrow(expensePie, Priority.ALWAYS);
        HBox.setHgrow(incomePie, Priority.ALWAYS);
        HBox pies = new HBox(20, expensePie, incomePie);

        // --- Évolution du solde --------------------------------------------
        VBox evolutionCard = buildEvolution(service);

        content.getChildren().addAll(title, highlights, pies, evolutionCard);
        setContent(content);
    }

    private VBox pieCard(String titleText, Map<String, Double> data) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        Label t = new Label(titleText);
        t.getStyleClass().add("card-title");
        card.getChildren().add(t);

        if (data.isEmpty()) {
            Label empty = new Label("Aucune donnée.");
            empty.getStyleClass().add("muted");
            card.getChildren().add(empty);
            return card;
        }

        PieChart pie = new PieChart();
        for (Map.Entry<String, Double> e : data.entrySet()) {
            pie.getData().add(new PieChart.Data(e.getKey(), e.getValue()));
        }
        pie.setLegendVisible(true);
        pie.setLabelsVisible(true);
        pie.setPrefHeight(300);
        card.getChildren().add(pie);
        return card;
    }

    private VBox buildEvolution(WalletService service) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        Label t = new Label("Évolution du solde");
        t.getStyleClass().add("card-title");
        card.getChildren().add(t);

        Map<String, Double> evolution = service.balanceEvolution();
        if (evolution.isEmpty()) {
            Label empty = new Label("Aucune donnée.");
            empty.getStyleClass().add("muted");
            card.getChildren().add(empty);
            return card;
        }

        CategoryAxis x = new CategoryAxis();
        x.setLabel("Date");
        NumberAxis y = new NumberAxis();
        y.setLabel("Solde");

        LineChart<String, Number> chart = new LineChart<>(x, y);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(true);
        chart.setPrefHeight(320);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Double> e : evolution.entrySet()) {
            series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }
        chart.getData().add(series);
        card.getChildren().add(chart);
        return card;
    }

    /** Transforme "2026-03" en "mars 2026". */
    private String prettyMonth(String yearMonth) {
        try {
            YearMonth ym = YearMonth.parse(yearMonth);
            return ym.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH)
                    + " " + ym.getYear();
        } catch (Exception e) {
            return yearMonth;
        }
    }
}
