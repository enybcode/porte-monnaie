package com.portemonnaie.ui.pages;

import com.portemonnaie.model.Category;
import com.portemonnaie.model.Transaction;
import com.portemonnaie.model.TransactionType;
import com.portemonnaie.service.ExportService;
import com.portemonnaie.service.TransactionFilter;
import com.portemonnaie.service.WalletService;
import com.portemonnaie.ui.App;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Page "Transactions" : historique complet, recherche/filtres, modification,
 * suppression et export (CSV / PDF) de la liste affichée.
 */
public class TransactionsPage extends BorderPane {

    private static final String ALL_TYPES = "Tous";
    private static final String ALL_CATS = "Toutes";

    private final App app;
    private final WalletService service;
    private final ExportService exportService = new ExportService();

    private final TableView<Transaction> table = new TableView<>();
    private final ObservableList<Transaction> rows = FXCollections.observableArrayList();

    // Contrôles de filtre
    private final ComboBox<String> typeFilter = new ComboBox<>();
    private final ComboBox<String> categoryFilter = new ComboBox<>();
    private final TextField keywordField = new TextField();
    private final DatePicker fromDate = new DatePicker();
    private final DatePicker toDate = new DatePicker();
    private final TextField minAmount = new TextField();
    private final TextField maxAmount = new TextField();

    public TransactionsPage(App app, WalletService service) {
        this.app = app;
        this.service = service;
        getStyleClass().add("page");
        setPadding(new Insets(28));

        setTop(buildHeaderAndFilters());
        setCenter(buildTable());
        setBottom(buildActions());

        applyFilters(); // chargement initial (toutes les transactions)
    }

    // ----------------------------- En-tête + filtres -----------------------
    private VBox buildHeaderAndFilters() {
        Label title = new Label("Transactions");
        title.getStyleClass().add("page-title");

        // Type
        typeFilter.getItems().addAll(ALL_TYPES,
                TransactionType.REVENU.getLabel(), TransactionType.DEPENSE.getLabel());
        typeFilter.setValue(ALL_TYPES);

        // Catégorie
        categoryFilter.getItems().add(ALL_CATS);
        for (Category c : service.getCategories()) {
            categoryFilter.getItems().add(c.getName());
        }
        categoryFilter.setValue(ALL_CATS);

        keywordField.setPromptText("Mot-clé...");
        fromDate.setPromptText("Du");
        toDate.setPromptText("Au");
        minAmount.setPromptText("Montant min");
        maxAmount.setPromptText("Montant max");

        keywordField.setPrefWidth(150);
        minAmount.setPrefWidth(110);
        maxAmount.setPrefWidth(110);

        Button apply = new Button("Filtrer");
        apply.getStyleClass().add("primary-button");
        apply.setOnAction(e -> applyFilters());

        Button reset = new Button("Réinitialiser");
        reset.getStyleClass().add("ghost-button");
        reset.setOnAction(e -> resetFilters());

        FlowPane filters = new FlowPane(10, 10,
                labeled("Type", typeFilter),
                labeled("Catégorie", categoryFilter),
                labeled("Mot-clé", keywordField),
                labeled("Du", fromDate),
                labeled("Au", toDate),
                labeled("Min", minAmount),
                labeled("Max", maxAmount),
                bottomAligned(apply),
                bottomAligned(reset));
        filters.getStyleClass().add("card");
        filters.setPadding(new Insets(16));

        VBox box = new VBox(14, title, filters);
        box.setPadding(new Insets(0, 0, 14, 0));
        return box;
    }

    private VBox labeled(String text, Region field) {
        Label l = new Label(text);
        l.getStyleClass().add("field-label");
        return new VBox(4, l, field);
    }

    private VBox bottomAligned(Button b) {
        Region spacer = new Region();
        spacer.setMinHeight(18);
        return new VBox(4, spacer, b);
    }

    // ----------------------------- Tableau ---------------------------------
    private TableView<Transaction> buildTable() {
        TableColumn<Transaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getDate().toString()));
        dateCol.setPrefWidth(110);

        TableColumn<Transaction, String> titleCol = new TableColumn<>("Titre");
        titleCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        titleCol.setPrefWidth(220);

        TableColumn<Transaction, String> catCol = new TableColumn<>("Catégorie");
        catCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));
        catCol.setPrefWidth(140);

        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getType().getLabel()));
        typeCol.setPrefWidth(100);

        TableColumn<Transaction, Transaction> amountCol = new TableColumn<>("Montant");
        amountCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        amountCol.setPrefWidth(150);
        amountCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Transaction t, boolean empty) {
                super.updateItem(t, empty);
                getStyleClass().removeAll("income", "expense");
                if (empty || t == null) {
                    setText(null);
                } else {
                    boolean income = t.getType() == TransactionType.REVENU;
                    setText((income ? "+" : "-") + service.formatMoney(t.getAmount()));
                    getStyleClass().add(income ? "income" : "expense");
                }
            }
        });

        table.getColumns().add(dateCol);
        table.getColumns().add(titleCol);
        table.getColumns().add(catCol);
        table.getColumns().add(typeCol);
        table.getColumns().add(amountCol);

        table.setItems(rows);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Aucune transaction ne correspond."));

        // Double-clic = modifier
        table.setRowFactory(tv -> {
            javafx.scene.control.TableRow<Transaction> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    app.showAddTransaction(row.getItem());
                }
            });
            return row;
        });

        return table;
    }

    // ----------------------------- Actions ---------------------------------
    private HBox buildActions() {
        Button add = new Button("+ Ajouter");
        add.getStyleClass().add("primary-button");
        add.setOnAction(e -> app.showAddTransaction(null));

        Button edit = new Button("Modifier");
        edit.getStyleClass().add("ghost-button");
        edit.setOnAction(e -> {
            Transaction sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { info("Sélectionnez d'abord une transaction."); return; }
            app.showAddTransaction(sel);
        });

        Button delete = new Button("Supprimer");
        delete.getStyleClass().add("danger-button");
        delete.setOnAction(e -> deleteSelected());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button csv = new Button("Exporter CSV");
        csv.getStyleClass().add("ghost-button");
        csv.setOnAction(e -> exportCsv());

        Button pdf = new Button("Exporter PDF");
        pdf.getStyleClass().add("ghost-button");
        pdf.setOnAction(e -> exportPdf());

        HBox bar = new HBox(10, add, edit, delete, spacer, csv, pdf);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 0, 0, 0));
        return bar;
    }

    private void deleteSelected() {
        Transaction sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { info("Sélectionnez d'abord une transaction."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer « " + sel.getTitle() + " » ? Cette action est définitive.",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirmer la suppression");
        confirm.setTitle("Porte-Monnaie");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            service.deleteTransaction(sel.getId());
            applyFilters();
        }
    }

    // ----------------------------- Filtrage --------------------------------
    private void applyFilters() {
        TransactionFilter f = new TransactionFilter();

        if (!ALL_TYPES.equals(typeFilter.getValue())) {
            f.type = TransactionType.fromLabel(typeFilter.getValue());
        }
        if (!ALL_CATS.equals(categoryFilter.getValue())) {
            f.category = categoryFilter.getValue();
        }
        f.keyword = keywordField.getText();
        f.dateFrom = fromDate.getValue();
        f.dateTo = toDate.getValue();
        f.amountMin = parseAmount(minAmount.getText());
        f.amountMax = parseAmount(maxAmount.getText());

        List<Transaction> result = service.search(f);
        rows.setAll(result);
    }

    private void resetFilters() {
        typeFilter.setValue(ALL_TYPES);
        categoryFilter.setValue(ALL_CATS);
        keywordField.clear();
        fromDate.setValue(null);
        toDate.setValue(null);
        minAmount.clear();
        maxAmount.clear();
        applyFilters();
    }

    private Double parseAmount(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return Double.parseDouble(raw.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    // ----------------------------- Export ----------------------------------
    private void exportCsv() {
        if (rows.isEmpty()) { info("Rien à exporter."); return; }
        FileChooser fc = new FileChooser();
        fc.setTitle("Exporter en CSV");
        fc.setInitialFileName("transactions.csv");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier CSV", "*.csv"));
        File file = fc.showSaveDialog(getScene().getWindow());
        if (file == null) return;
        try {
            exportService.exportCsv(rows, file);
            info("Export CSV réussi :\n" + file.getAbsolutePath());
        } catch (Exception ex) {
            error("Échec de l'export CSV : " + ex.getMessage());
        }
    }

    private void exportPdf() {
        if (rows.isEmpty()) { info("Rien à exporter."); return; }
        FileChooser fc = new FileChooser();
        fc.setTitle("Exporter en PDF");
        fc.setInitialFileName("transactions.pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier PDF", "*.pdf"));
        File file = fc.showSaveDialog(getScene().getWindow());
        if (file == null) return;
        try {
            exportService.exportPdf(rows, file, service.getUserName(), service.getCurrency());
            info("Export PDF réussi :\n" + file.getAbsolutePath());
        } catch (Exception ex) {
            error("Échec de l'export PDF : " + ex.getMessage());
        }
    }

    // ----------------------------- Dialogues -------------------------------
    private void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg);
        a.setHeaderText(null);
        a.setTitle("Porte-Monnaie");
        a.showAndWait();
    }

    private void error(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        a.setHeaderText(null);
        a.setTitle("Porte-Monnaie");
        a.showAndWait();
    }
}
