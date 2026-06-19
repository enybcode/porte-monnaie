package com.portemonnaie.ui.pages;

import com.portemonnaie.model.Category;
import com.portemonnaie.service.WalletService;
import com.portemonnaie.ui.App;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Optional;

/**
 * Page "Catégories" : liste des catégories par défaut + personnalisées,
 * ajout d'une nouvelle catégorie, suppression des catégories personnalisées.
 */
public class CategoriesPage extends ScrollPane {

    private final App app;
    private final WalletService service;

    public CategoriesPage(App app, WalletService service) {
        this.app = app;
        this.service = service;
        getStyleClass().add("page-scroll");
        setFitToWidth(true);

        VBox content = new VBox(18);
        content.setPadding(new Insets(28));
        content.setMaxWidth(640);

        Label title = new Label("Catégories");
        title.getStyleClass().add("page-title");

        // --- Ajout ----------------------------------------------------------
        VBox addCard = new VBox(10);
        addCard.getStyleClass().add("card");
        Label addTitle = new Label("Ajouter une catégorie");
        addTitle.getStyleClass().add("card-title");

        TextField nameField = new TextField();
        nameField.setPromptText("Nom de la nouvelle catégorie");
        HBox.setHgrow(nameField, Priority.ALWAYS);

        Button addBtn = new Button("Ajouter");
        addBtn.getStyleClass().add("primary-button");
        addBtn.setOnAction(e -> {
            String name = nameField.getText() == null ? "" : nameField.getText().trim();
            if (name.isEmpty()) {
                warn("Veuillez saisir un nom.");
                return;
            }
            boolean ok = service.addCategory(name);
            if (!ok) {
                warn("Cette catégorie existe déjà.");
                return;
            }
            app.showCategories(); // recharge la page (liste à jour)
        });

        HBox addRow = new HBox(10, nameField, addBtn);
        addRow.setAlignment(Pos.CENTER_LEFT);
        addCard.getChildren().addAll(addTitle, addRow);

        // --- Liste ----------------------------------------------------------
        VBox listCard = new VBox(8);
        listCard.getStyleClass().add("card");
        Label listTitle = new Label("Vos catégories");
        listTitle.getStyleClass().add("card-title");
        listCard.getChildren().add(listTitle);

        for (Category c : service.getCategories()) {
            listCard.getChildren().add(buildRow(c));
        }

        content.getChildren().addAll(title, addCard, listCard);
        setContent(content);
    }

    private HBox buildRow(Category c) {
        HBox row = new HBox(12);
        row.getStyleClass().add("tx-row");
        row.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label(c.getName());
        name.getStyleClass().add("tx-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(name, spacer);

        if (c.isDefaultCategory()) {
            Label badge = new Label("Par défaut");
            badge.getStyleClass().add("badge");
            row.getChildren().add(badge);
        } else {
            Button del = new Button("Supprimer");
            del.getStyleClass().add("danger-button");
            del.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Supprimer la catégorie « " + c.getName() + " » ?",
                        ButtonType.YES, ButtonType.NO);
                confirm.setHeaderText("Confirmer");
                confirm.setTitle("Porte-Monnaie");
                Optional<ButtonType> r = confirm.showAndWait();
                if (r.isPresent() && r.get() == ButtonType.YES) {
                    service.deleteCategory(c.getId());
                    app.showCategories();
                }
            });
            row.getChildren().add(del);
        }
        return row;
    }

    private void warn(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg);
        a.setHeaderText(null);
        a.setTitle("Porte-Monnaie");
        a.showAndWait();
    }
}
