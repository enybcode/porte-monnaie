package com.portemonnaie.service;

import com.portemonnaie.model.TransactionType;
import java.time.LocalDate;

/**
 * Critères de recherche/filtre des transactions.
 * Tous les champs sont optionnels : un champ "null" signifie "pas de filtre".
 * On construit l'objet puis on le passe au DAO qui assemble le SQL.
 */
public class TransactionFilter {

    public TransactionType type;     // REVENU / DEPENSE / null
    public String category;          // nom de catégorie / null
    public String keyword;           // recherche dans titre + description / null
    public LocalDate dateFrom;       // date min / null
    public LocalDate dateTo;         // date max / null
    public Double amountMin;         // montant min / null
    public Double amountMax;         // montant max / null

    public boolean isEmpty() {
        return type == null && category == null
            && (keyword == null || keyword.isBlank())
            && dateFrom == null && dateTo == null
            && amountMin == null && amountMax == null;
    }
}
