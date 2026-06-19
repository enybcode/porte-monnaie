package com.portemonnaie.model;

/**
 * Type d'une transaction : une entrée d'argent (REVENU) ou une sortie (DEPENSE).
 * Chaque valeur porte un libellé lisible affiché dans l'interface.
 */
public enum TransactionType {
    REVENU("Revenu"),
    DEPENSE("Dépense");

    private final String label;

    TransactionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Retrouve le type à partir de son libellé affiché (ex: "Revenu"). */
    public static TransactionType fromLabel(String label) {
        for (TransactionType t : values()) {
            if (t.label.equalsIgnoreCase(label)) {
                return t;
            }
        }
        return DEPENSE;
    }

    @Override
    public String toString() {
        return label;
    }
}
