package com.portemonnaie.ui.pages;

import com.portemonnaie.service.WalletService;
import com.portemonnaie.ui.App;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.Optional;

/**
 * Page "Paramètres" : nom de l'utilisateur, devise, réinitialisation des
 * données (avec confirmation) et informations sur l'application.
 */
public class SettingsPage extends ScrollPane {

    public SettingsPage(App app, WalletService service) {
        getStyleClass().add("page-scroll");
        setFitToWidth(true);

        VBox content = new VBox(18);
        content.setPadding(new Insets(28));
        content.setMaxWidth(560);

        Label title = new Label("Paramètres");
        title.getStyleClass().add("page-title");

        // --- Profil ---------------------------------------------------------
        VBox profileCard = new VBox(12);
        profileCard.getStyleClass().add("card");
        Label profileTitle = new Label("Profil");
        profileTitle.getStyleClass().add("card-title");

        TextField nameField = new TextField(service.getUserName());

        ComboBox<String> currencyBox = new ComboBox<>();
        currencyBox.getItems().addAll("€", "$", "£");
        currencyBox.setValue(service.getCurrency());
        currencyBox.setMaxWidth(Double.MAX_VALUE);

        Button save = new Button("Enregistrer");
        save.getStyleClass().add("primary-button");
        save.setOnAction(e -> {
            String name = nameField.getText() == null ? "" : nameField.getText().trim();
            if (name.isEmpty()) name = "Utilisateur";
            service.setUserName(name);
            service.setCurrency(currencyBox.getValue());
            app.refreshSidebar();
            info("Paramètres enregistrés.");
            app.showSettings();
        });

        profileCard.getChildren().addAll(
                profileTitle,
                labeled("Nom de l'utilisateur", nameField),
                labeled("Devise", currencyBox),
                save);

        // --- Données --------------------------------------------------------
        VBox dataCard = new VBox(10);
        dataCard.getStyleClass().add("card");
        Label dataTitle = new Label("Données");
        dataTitle.getStyleClass().add("card-title");
        Label dataInfo = new Label(
                "Réinitialiser supprime toutes les transactions et les catégories "
                + "personnalisées. Les catégories par défaut sont conservées.");
        dataInfo.getStyleClass().add("muted");
        dataInfo.setWrapText(true);

        Button reset = new Button("Réinitialiser les données");
        reset.getStyleClass().add("danger-button");
        reset.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Êtes-vous sûr de vouloir tout effacer ? Cette action est définitive.",
                    ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText("Réinitialiser les données");
            confirm.setTitle("Porte-Monnaie");
            Optional<ButtonType> r = confirm.showAndWait();
            if (r.isPresent() && r.get() == ButtonType.YES) {
                service.resetData();
                info("Toutes les données ont été réinitialisées.");
                app.showDashboard();
            }
        });
        dataCard.getChildren().addAll(dataTitle, dataInfo, reset);

        // --- À propos -------------------------------------------------------
        VBox aboutCard = new VBox(6);
        aboutCard.getStyleClass().add("card");
        Label aboutTitle = new Label("À propos");
        aboutTitle.getStyleClass().add("card-title");
        Label l1 = new Label("Porte-Monnaie — gestion de budget personnel");
        Label l2 = new Label("Version 1.0");
        Label l3 = new Label("Technologies : Java, JavaFX, SQLite");
        Label l4 = new Label("Les données sont stockées localement sur votre ordinateur.");
        l2.getStyleClass().add("muted");
        l3.getStyleClass().add("muted");
        l4.getStyleClass().add("muted");
        aboutCard.getChildren().addAll(aboutTitle, l1, l2, l3, l4);

        content.getChildren().addAll(title, profileCard, dataCard, aboutCard);
        setContent(content);
    }

    private VBox labeled(String text, javafx.scene.Node field) {
        Label l = new Label(text);
        l.getStyleClass().add("field-label");
        return new VBox(5, l, field);
    }

    private void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg);
        a.setHeaderText(null);
        a.setTitle("Porte-Monnaie");
        a.showAndWait();
    }
}
