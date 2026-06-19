package com.portemonnaie;

import com.portemonnaie.ui.App;
import javafx.application.Application;

/**
 * Point d'entrée du programme.
 *
 * On utilise une classe "lanceur" séparée (qui n'hérite PAS de Application).
 * C'est la méthode recommandée pour éviter l'erreur fréquente
 * "JavaFX runtime components are missing" lorsqu'on lance l'application
 * autrement que via le plugin Maven.
 */
public class Main {
    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}
