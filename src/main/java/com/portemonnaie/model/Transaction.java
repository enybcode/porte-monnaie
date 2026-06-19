package com.portemonnaie.model;

import java.time.LocalDate;

/**
 * Une transaction = une ligne d'argent qui entre ou qui sort.
 *
 * Champs :
 *  - id          : identifiant unique en base (0 tant que non enregistré)
 *  - title       : titre court (ex: "Courses Carrefour")
 *  - amount      : montant POSITIF (le sens dépend du type)
 *  - type        : REVENU ou DEPENSE
 *  - category    : nom de la catégorie
 *  - date        : date de l'opération
 *  - description : note optionnelle (peut être vide)
 */
public class Transaction {

    private int id;
    private String title;
    private double amount;
    private TransactionType type;
    private String category;
    private LocalDate date;
    private String description;

    public Transaction(int id, String title, double amount, TransactionType type,
                       String category, LocalDate date, String description) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date;
        this.description = description == null ? "" : description;
    }

    /** Constructeur pratique pour une transaction pas encore enregistrée (id = 0). */
    public Transaction(String title, double amount, TransactionType type,
                       String category, LocalDate date, String description) {
        this(0, title, amount, type, category, date, description);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    /** Montant signé : positif pour un revenu, négatif pour une dépense. */
    public double getSignedAmount() {
        return type == TransactionType.REVENU ? amount : -amount;
    }
}
