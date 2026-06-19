package com.portemonnaie.ui.pages;

import com.portemonnaie.model.Goal;
import com.portemonnaie.service.WalletService;
import com.portemonnaie.ui.App;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class GoalsPage extends ScrollPane {
    public GoalsPage(App app, WalletService service) {
        getStyleClass().add("page-scroll");
        setFitToWidth(true);

        VBox content = new VBox(20);
        content.setPadding(new Insets(28));

        Label title = new Label("Epargne");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Preparez vos projets et suivez votre progression.");
        subtitle.getStyleClass().add("page-subtitle");

        VBox form = buildForm(app, service);
        VBox list = new VBox(12);
        list.getStyleClass().add("goals-list");

        if (service.getGoals().isEmpty()) {
            VBox empty = new VBox(8);
            empty.getStyleClass().add("card");
            Label e = new Label("Aucun objectif pour le moment.");
            e.getStyleClass().add("card-title");
            Label hint = new Label("Exemples : acheter un PC, financer un voyage, constituer un fonds d'urgence.");
            hint.getStyleClass().add("muted");
            hint.setWrapText(true);
            empty.getChildren().addAll(e, hint);
            list.getChildren().add(empty);
        } else {
            for (Goal goal : service.getGoals()) {
                list.getChildren().add(goalCard(app, service, goal));
            }
        }

        content.getChildren().addAll(new VBox(2, title, subtitle), form, list);
        setContent(content);
    }

    private VBox buildForm(App app, WalletService service) {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");

        Label title = new Label("Nouvel objectif");
        title.getStyleClass().add("card-title");

        TextField name = new TextField();
        name.setPromptText("Ex : Acheter un PC");
        TextField target = new TextField();
        target.setPromptText("Montant cible");
        TextField current = new TextField();
        current.setPromptText("Montant actuel");

        Button add = new Button("Ajouter l'objectif");
        add.getStyleClass().add("primary-button");
        add.setOnAction(e -> {
            String n = name.getText() == null ? "" : name.getText().trim();
            Double targetAmount = parseAmount(target.getText());
            Double currentAmount = parseAmount(current.getText());
            if (n.isBlank() || targetAmount == null || targetAmount <= 0) {
                warn("Indiquez un titre et un montant cible valide.");
                return;
            }
            service.addGoal(new Goal(n, targetAmount, currentAmount == null ? 0 : Math.max(0, currentAmount)));
            app.showGoals();
        });

        HBox fields = new HBox(10, labeled("Projet", name), labeled("Objectif", target), labeled("Actuel", current), bottom(add));
        fields.setAlignment(Pos.BOTTOM_LEFT);
        card.getChildren().addAll(title, fields);
        return card;
    }

    private VBox goalCard(App app, WalletService service, Goal goal) {
        VBox card = new VBox(10);
        card.getStyleClass().add("goal-card");

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label(goal.getTitle());
        title.getStyleClass().add("card-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label amount = new Label(service.formatMoney(goal.getCurrentAmount()) + " / " + service.formatMoney(goal.getTargetAmount()));
        amount.getStyleClass().add("stat-value-small");
        top.getChildren().addAll(title, spacer, amount);

        ProgressBar progress = new ProgressBar(goal.getProgress());
        progress.setMaxWidth(Double.MAX_VALUE);
        progress.getStyleClass().add("goal-progress");

        Label remaining = new Label(goal.getRemainingAmount() == 0
                ? "Objectif atteint"
                : "Reste a economiser : " + service.formatMoney(goal.getRemainingAmount()));
        remaining.getStyleClass().add("muted");

        TextField addAmount = new TextField();
        addAmount.setPromptText("Ajouter une somme");
        addAmount.setPrefWidth(150);
        Button save = new Button("Ajouter");
        save.getStyleClass().add("ghost-button");
        save.setOnAction(e -> {
            Double value = parseAmount(addAmount.getText());
            if (value == null || value <= 0) {
                warn("Saisissez un montant positif.");
                return;
            }
            goal.setCurrentAmount(goal.getCurrentAmount() + value);
            service.updateGoal(goal);
            app.showGoals();
        });
        Button delete = new Button("Supprimer");
        delete.getStyleClass().add("danger-button");
        delete.setOnAction(e -> {
            service.deleteGoal(goal.getId());
            app.showGoals();
        });
        HBox actions = new HBox(10, addAmount, save, delete);
        actions.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(top, progress, remaining, actions);
        return card;
    }

    private VBox labeled(String text, javafx.scene.Node field) {
        Label label = new Label(text);
        label.getStyleClass().add("field-label");
        VBox box = new VBox(5, label, field);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private VBox bottom(Button b) {
        Region spacer = new Region();
        spacer.setMinHeight(18);
        return new VBox(5, spacer, b);
    }

    private Double parseAmount(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return Double.parseDouble(raw.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void warn(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message);
        alert.setTitle("Porte-Monnaie");
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
