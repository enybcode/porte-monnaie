package com.portemonnaie.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gère la base de données SQLite : connexion unique, création des tables
 * et insertion des données par défaut (catégories + réglages).
 *
 * Le fichier de base est stocké dans le dossier personnel de l'utilisateur :
 *   ~/.porte-monnaie/porte-monnaie.db
 * Ainsi les données ne dépendent pas de l'endroit d'où l'application est lancée
 * et ne disparaissent jamais à la fermeture.
 */
public class Database {

    private static Database instance;
    private Connection connection;

    private Database() {
        connect();
        createSchema();
        seedDefaults();
    }

    /** Point d'accès unique (singleton). */
    public static synchronized Database get() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    // --- Connexion -----------------------------------------------------------
    private void connect() {
        try {
            // Dossier de données dans le home de l'utilisateur
            File dir = new File(System.getProperty("user.home"), ".porte-monnaie");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File dbFile = new File(dir, "porte-monnaie.db");

            // Chargement explicite du driver (sécurité ; auto-chargé sinon)
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException ignored) {
                // Le driver moderne s'enregistre tout seul via le classpath.
            }

            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);

            // Active les clés étrangères (bonne pratique)
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON;");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Impossible d'ouvrir la base de données", e);
        }
    }

    // --- Création des tables -------------------------------------------------
    private void createSchema() {
        String categories = """
            CREATE TABLE IF NOT EXISTS categories (
                id         INTEGER PRIMARY KEY AUTOINCREMENT,
                name       TEXT NOT NULL UNIQUE,
                is_default INTEGER NOT NULL DEFAULT 0
            );
            """;

        String transactions = """
            CREATE TABLE IF NOT EXISTS transactions (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                title       TEXT NOT NULL,
                amount      REAL NOT NULL,
                type        TEXT NOT NULL,
                category    TEXT NOT NULL,
                date        TEXT NOT NULL,
                description TEXT
            );
            """;

        String users = """
            CREATE TABLE IF NOT EXISTS users (
                id            INTEGER PRIMARY KEY AUTOINCREMENT,
                first_name    TEXT NOT NULL,
                last_name     TEXT NOT NULL,
                email         TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL,
                salt          TEXT NOT NULL,
                created_at    TEXT NOT NULL
            );
            """;

        String goals = """
            CREATE TABLE IF NOT EXISTS goals (
                id             INTEGER PRIMARY KEY AUTOINCREMENT,
                title          TEXT NOT NULL,
                target_amount  REAL NOT NULL,
                current_amount REAL NOT NULL DEFAULT 0,
                created_at     TEXT NOT NULL
            );
            """;

        String settings = """
            CREATE TABLE IF NOT EXISTS settings (
                key   TEXT PRIMARY KEY,
                value TEXT
            );
            """;

        try (Statement st = connection.createStatement()) {
            st.execute(users);
            st.execute(categories);
            st.execute(transactions);
            st.execute(goals);
            st.execute(settings);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de création du schéma", e);
        }
    }

    // --- Données par défaut --------------------------------------------------
    private void seedDefaults() {
        String[] defaultCategories = {
            "Nourriture", "Transport", "Loisirs", "Salaire",
            "École", "Abonnement", "Santé", "Autre"
        };

        try (Statement st = connection.createStatement()) {
            // Catégories par défaut (INSERT OR IGNORE => ne duplique jamais)
            for (String name : defaultCategories) {
                st.executeUpdate(
                    "INSERT OR IGNORE INTO categories(name, is_default) VALUES('"
                        + name.replace("'", "''") + "', 1);");
            }
            // Réglages par défaut
            st.executeUpdate("INSERT OR IGNORE INTO settings(key, value) "
                + "VALUES('user_name', 'Utilisateur');");
            st.executeUpdate("INSERT OR IGNORE INTO settings(key, value) "
                + "VALUES('currency', '€');");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur d'initialisation des données", e);
        }
    }
}
