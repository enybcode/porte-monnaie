package com.portemonnaie.ui.pages;

import com.portemonnaie.service.AuthService;
import com.portemonnaie.ui.App;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class LoginPage extends VBox {
    private boolean registerMode = false;

    private final TextField firstName = new TextField();
    private final TextField lastName = new TextField();
    private final TextField email = new TextField();
    private final PasswordField password = new PasswordField();
    private final PasswordField confirm = new PasswordField();

    private final VBox nameFields;
    private final VBox confirmField;
    private final Label title = new Label();
    private final Label subtitle = new Label();
    private final Button submit = new Button();
    private final Button switchMode = new Button();

    public LoginPage(App app, AuthService authService) {
        getStyleClass().add("login-page");
        setAlignment(Pos.CENTER);
        setPadding(new Insets(36));

        VBox card = new VBox(16);
        card.getStyleClass().add("wallet-login-card");
        card.setMaxWidth(460);

        Label brand = new Label("Porte-Monnaie");
        brand.getStyleClass().add("login-brand");
        title.getStyleClass().add("page-title");
        subtitle.getStyleClass().add("page-subtitle");

        firstName.setPromptText("Prenom");
        lastName.setPromptText("Nom");
        email.setPromptText("Email");
        password.setPromptText("Mot de passe");
        confirm.setPromptText("Confirmer le mot de passe");

        HBox names = new HBox(10, firstName, lastName);
        nameFields = labeled("Identite", names);
        confirmField = labeled("Confirmation", confirm);

        submit.getStyleClass().add("primary-button");
        submit.setMaxWidth(Double.MAX_VALUE);
        submit.setOnAction(e -> submit(app, authService));

        switchMode.getStyleClass().add("link-button");
        switchMode.setOnAction(e -> {
            registerMode = !registerMode;
            refreshMode();
        });

        Region stitch = new Region();
        stitch.getStyleClass().add("stitch-line");

        card.getChildren().addAll(
                brand, stitch, title, subtitle,
                nameFields,
                labeled("Email", email),
                labeled("Mot de passe", password),
                confirmField,
                submit,
                switchMode);

        getChildren().add(card);
        refreshMode();
    }

    private VBox labeled(String text, javafx.scene.Node field) {
        Label label = new Label(text);
        label.getStyleClass().add("field-label");
        return new VBox(5, label, field);
    }

    private void refreshMode() {
        nameFields.setVisible(registerMode);
        nameFields.setManaged(registerMode);
        confirmField.setVisible(registerMode);
        confirmField.setManaged(registerMode);
        if (registerMode) {
            title.setText("Creer votre coffre");
            subtitle.setText("Un compte local protege par mot de passe pour vos finances.");
            submit.setText("Creer le compte");
            switchMode.setText("J'ai deja un compte");
        } else {
            title.setText("Acces au portefeuille");
            subtitle.setText("Connectez-vous pour ouvrir votre espace budget.");
            submit.setText("Se connecter");
            switchMode.setText("Creer un compte");
        }
    }

    private void submit(App app, AuthService authService) {
        String mail = email.getText() == null ? "" : email.getText().trim();
        String pass = password.getText() == null ? "" : password.getText();
        if (!mail.contains("@") || pass.length() < 4) {
            warn("Saisissez un email valide et un mot de passe de 4 caracteres minimum.");
            return;
        }

        if (registerMode) {
            String first = firstName.getText() == null ? "" : firstName.getText().trim();
            String last = lastName.getText() == null ? "" : lastName.getText().trim();
            if (first.isBlank() || last.isBlank()) {
                warn("Le prenom et le nom sont obligatoires.");
                return;
            }
            if (!pass.equals(confirm.getText())) {
                warn("Les deux mots de passe ne correspondent pas.");
                return;
            }
            if (!authService.register(first, last, mail, pass)) {
                warn("Un compte existe deja avec cet email.");
                return;
            }
            app.onAuthenticated(authService.getCurrentUser());
        } else {
            if (!authService.login(mail, pass)) {
                warn("Email ou mot de passe incorrect.");
                return;
            }
            app.onAuthenticated(authService.getCurrentUser());
        }
    }

    private void warn(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message);
        alert.setTitle("Porte-Monnaie");
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
