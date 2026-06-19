package com.portemonnaie.ui.components;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Carte "statistique" : un petit compartiment du portefeuille affichant
 * un libellé (en haut) et une valeur mise en avant (en dessous).
 */
public class StatCard extends VBox {

    private final Label valueLabel;

    public StatCard(String title, String value) {
        getStyleClass().add("stat-card");
        setSpacing(6);

        Label titleLabel = new Label(title.toUpperCase());
        titleLabel.getStyleClass().add("stat-title");

        valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");

        getChildren().addAll(titleLabel, valueLabel);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    /** Ajoute une classe de style à la valeur (ex: "income" ou "expense"). */
    public void emphasize(String styleClass) {
        valueLabel.getStyleClass().add(styleClass);
    }
}
