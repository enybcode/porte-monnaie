package com.portemonnaie.ui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Button;

/**
 * Bouton de la barre de navigation latérale.
 * Sait s'afficher comme "sélectionné" (page active).
 */
public class NavButton extends Button {

    public NavButton(String text) {
        super(text);
        getStyleClass().add("nav-button");
        setMaxWidth(Double.MAX_VALUE);
        setAlignment(Pos.CENTER_LEFT);
    }

    public void setSelected(boolean selected) {
        getStyleClass().remove("nav-button-selected");
        if (selected) {
            getStyleClass().add("nav-button-selected");
        }
    }
}
