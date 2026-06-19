package com.portemonnaie.ui.pages;

import com.portemonnaie.model.Category;
import com.portemonnaie.model.Transaction;
import com.portemonnaie.model.TransactionType;
import com.portemonnaie.service.WalletService;
import com.portemonnaie.ui.App;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

/**
 * Formulaire d'ajout OU de modification d'une transaction.
 * Si "editing" est null on crée une nouvelle transaction,
 * sinon on pré-remplit le formulaire pour la modifier.
 */
public class AddTransactionPage extends ScrollPane {

    public AddTransactionPage(App app, WalletService service, Transaction editing) {
        getStyleClass().add("page-scroll");
        setFitToWidth(true);

        boolean isEdit = editing != null;

        VBox content = new VBox(18);
        content.setPadding(new Insets(28));
        content.setMaxWidth(560);

        Label title = new Label(isEdit ? "Modifier la transaction" : "Ajouter une transaction");
        title.getStyleClass().add("page-title");

        VBox form = new VBox(14);
        form.getStyleClass().add("card");

        // Champ : Titre
        TextField titleField = new TextField();
        titleField.setPromptText("Ex : Courses, Salaire, Cinéma...");

        // Champ : Montant
        TextField amountField = new TextField();
        amountField.setPromptText("Ex : 49,90");

        // Champ : Type
        ComboBox<TransactionType> typeBox = new ComboBox<>();
        typeBox.getItems().addAll(TransactionType.REVENU, TransactionType.DEPENSE);
        typeBox.setValue(TransactionType.DEPENSE);
        typeBox.setMaxWidth(Double.MAX_VALUE);

        // Champ : Catégorie
        ComboBox<String> categoryBox = new ComboBox<>();
        for (Category c : service.getCategories()) {
            categoryBox.getItems().add(c.getName());
        }
        if (!categoryBox.getItems().isEmpty()) {
            categoryBox.setValue(categoryBox.getItems().get(0));
        }
        categoryBox.setMaxWidth(Double.MAX_VALUE);

        // Champ : Date
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);

        // Champ : Description
        TextArea descArea = new TextArea();
        descArea.setPromptText("Note optionnelle...");
        descArea.setPrefRowCount(3);
        descArea.setWrapText(true);

        // Pré-remplissage si modification
        if (isEdit) {
            titleField.setText(editing.getTitle());
            amountField.setText(String.valueOf(editing.getAmount()).replace('.', ','));
            typeBox.setValue(editing.getType());
            if (!categoryBox.getItems().contains(editing.getCategory())) {
                categoryBox.getItems().add(editing.getCategory());
            }
            categoryBox.setValue(editing.getCategory());
            datePicker.setValue(editing.getDate());
            descArea.setText(editing.getDescription());
        }

        form.getChildren().addAll(
                labeled("Titre", titleField),
                labeled("Montant", amountField),
                labeled("Type", typeBox),
                labeled("Catégorie", categoryBox),
                labeled("Date", datePicker),
                labeled("Description (optionnel)", descArea)
        );

        // Boutons
        Button save = new Button(isEdit ? "Enregistrer les modifications" : "Ajouter");
        save.getStyleClass().add("primary-button");
        Button cancel = new Button("Annuler");
        cancel.getStyleClass().add("ghost-button");
        cancel.setOnAction(e -> app.showTransactions());

        save.setOnAction(e -> {
            // --- Validation ---
            String t = titleField.getText() == null ? "" : titleField.getText().trim();
            if (t.isEmpty()) {
                error("Le titre est obligatoire.");
                return;
            }
            Double amount = parseAmount(amountField.getText());
            if (amount == null || amount <= 0) {
                error("Le montant doit être un nombre positif (ex : 49,90).");
                return;
            }
            if (categoryBox.getValue() == null) {
                error("Veuillez choisir une catégorie.");
                return;
            }
            if (datePicker.getValue() == null) {
                error("Veuillez choisir une date.");
                return;
            }

            // --- Enregistrement ---
            Transaction tx = new Transaction(
                    t, amount, typeBox.getValue(), categoryBox.getValue(),
                    datePicker.getValue(),
                    descArea.getText() == null ? "" : descArea.getText().trim());

            if (isEdit) {
                tx.setId(editing.getId());
                service.updateTransaction(tx);
            } else {
                service.addTransaction(tx);
            }
            app.showTransactions();
        });

        HBox buttons = new HBox(12, save, cancel);

        content.getChildren().addAll(title, form, buttons);
        setContent(content);
    }

    /** Empile un libellé au-dessus d'un champ. */
    private VBox labeled(String labelText, javafx.scene.Node field) {
        Label l = new Label(labelText);
        l.getStyleClass().add("field-label");
        VBox box = new VBox(5, l, field);
        return box;
    }

    /** Convertit "49,90" ou "49.90" en nombre. Renvoie null si invalide. */
    private Double parseAmount(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return Double.parseDouble(raw.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void error(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message);
        alert.setHeaderText("Champ invalide");
        alert.setTitle("Porte-Monnaie");
        alert.showAndWait();
    }
}
