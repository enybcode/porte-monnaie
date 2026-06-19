package com.portemonnaie.ui.theme;

import javafx.scene.Scene;

/**
 * Petit utilitaire de thème.
 * L'essentiel du style "cuir" est défini dans le fichier CSS (resources/styles.css).
 * On garde ici la palette sous forme de constantes (pratique pour documenter
 * et réutiliser au besoin) et une méthode pour appliquer la feuille de style.
 */
public final class Theme {

    private Theme() { }

    // Palette cuir (référence — l'application des couleurs se fait via le CSS)
    public static final String BG            = "#1a130d";
    public static final String SIDEBAR       = "#241a11";
    public static final String CARD          = "#2e2117";
    public static final String LEATHER       = "#6b4423";
    public static final String LEATHER_LIGHT = "#8b5a2b";
    public static final String GOLD          = "#d4af37";
    public static final String TEXT          = "#f0e6d2";
    public static final String TEXT_MUTED    = "#a89878";
    public static final String INCOME        = "#8aa86f";
    public static final String EXPENSE       = "#c06a52";

    /** Applique la feuille de style cuir à une scène. */
    public static void apply(Scene scene) {
        var url = Theme.class.getResource("/styles.css");
        if (url != null) {
            scene.getStylesheets().add(url.toExternalForm());
        }
    }
}
