package com.portemonnaie.service;

import com.portemonnaie.dao.CategoryDao;
import com.portemonnaie.dao.SettingsDao;
import com.portemonnaie.dao.TransactionDao;
import com.portemonnaie.model.Category;
import com.portemonnaie.model.Transaction;
import com.portemonnaie.model.TransactionType;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Cœur logique de l'application : c'est la SEULE classe que l'interface
 * graphique utilise. Elle s'appuie sur les DAO et calcule tout ce dont
 * les pages ont besoin (totaux, statistiques, formatage des montants...).
 *
 * Cette séparation garantit que l'interface ne touche jamais directement
 * à la base de données.
 */
public class WalletService {

    private final TransactionDao transactionDao = new TransactionDao();
    private final CategoryDao categoryDao = new CategoryDao();
    private final SettingsDao settingsDao = new SettingsDao();

    // ====================== TRANSACTIONS (CRUD) ==========================
    public void addTransaction(Transaction t) { transactionDao.insert(t); }
    public void updateTransaction(Transaction t) { transactionDao.update(t); }
    public void deleteTransaction(int id) { transactionDao.delete(id); }

    public List<Transaction> getAllTransactions() {
        return transactionDao.find(new TransactionFilter());
    }

    public List<Transaction> search(TransactionFilter filter) {
        return transactionDao.find(filter);
    }

    public List<Transaction> getRecentTransactions(int limit) {
        return transactionDao.findRecent(limit);
    }

    // ====================== TOTAUX =======================================
    public double getTotalIncome() {
        return sumByType(TransactionType.REVENU);
    }

    public double getTotalExpense() {
        return sumByType(TransactionType.DEPENSE);
    }

    public double getBalance() {
        return getTotalIncome() - getTotalExpense();
    }

    private double sumByType(TransactionType type) {
        TransactionFilter f = new TransactionFilter();
        f.type = type;
        return transactionDao.find(f).stream()
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // ====================== RÉSUMÉ DU MOIS ===============================
    /** Renvoie [revenus, dépenses] pour le mois donné. */
    public double[] getMonthSummary(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        TransactionFilter f = new TransactionFilter();
        f.dateFrom = start;
        f.dateTo = end;

        double income = 0, expense = 0;
        for (Transaction t : transactionDao.find(f)) {
            if (t.getType() == TransactionType.REVENU) income += t.getAmount();
            else expense += t.getAmount();
        }
        return new double[]{income, expense};
    }

    // ====================== STATISTIQUES =================================
    /** Total des dépenses regroupées par catégorie (trié du + grand au + petit). */
    public Map<String, Double> expensesByCategory() {
        return groupByCategory(TransactionType.DEPENSE);
    }

    /** Total des revenus regroupés par catégorie. */
    public Map<String, Double> incomeByCategory() {
        return groupByCategory(TransactionType.REVENU);
    }

    private Map<String, Double> groupByCategory(TransactionType type) {
        TransactionFilter f = new TransactionFilter();
        f.type = type;
        Map<String, Double> tmp = new LinkedHashMap<>();
        for (Transaction t : transactionDao.find(f)) {
            tmp.merge(t.getCategory(), t.getAmount(), Double::sum);
        }
        // Tri décroissant par montant
        Map<String, Double> sorted = new LinkedHashMap<>();
        tmp.entrySet().stream()
           .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
           .forEach(e -> sorted.put(e.getKey(), e.getValue()));
        return sorted;
    }

    /**
     * Évolution du solde cumulé dans le temps.
     * Clé = date (texte), valeur = solde cumulé à cette date.
     */
    public Map<String, Double> balanceEvolution() {
        // On lit tout, trié par date croissante
        List<Transaction> all = transactionDao.find(new TransactionFilter());
        all.sort((a, b) -> a.getDate().compareTo(b.getDate()));

        Map<String, Double> evolution = new TreeMap<>(); // trié par date
        double running = 0;
        for (Transaction t : all) {
            running += t.getSignedAmount();
            evolution.put(t.getDate().toString(), running);
        }
        return evolution;
    }

    /** Mois où l'utilisateur a le plus dépensé. Renvoie null si aucune dépense. */
    public MonthStat mostExpensiveMonth() {
        TransactionFilter f = new TransactionFilter();
        f.type = TransactionType.DEPENSE;
        Map<String, Double> byMonth = new TreeMap<>();
        for (Transaction t : transactionDao.find(f)) {
            String key = YearMonth.from(t.getDate()).toString(); // ex 2026-03
            byMonth.merge(key, t.getAmount(), Double::sum);
        }
        String best = null;
        double max = -1;
        for (Map.Entry<String, Double> e : byMonth.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
                best = e.getKey();
            }
        }
        return best == null ? null : new MonthStat(best, max);
    }

    /** Catégorie où l'utilisateur dépense le plus. Renvoie null si aucune dépense. */
    public CategoryStat topExpenseCategory() {
        Map<String, Double> map = expensesByCategory();
        if (map.isEmpty()) return null;
        Map.Entry<String, Double> first = map.entrySet().iterator().next();
        return new CategoryStat(first.getKey(), first.getValue());
    }

    // ====================== CATÉGORIES ===================================
    public List<Category> getCategories() { return categoryDao.findAll(); }

    public boolean addCategory(String name) {
        if (name == null || name.isBlank()) return false;
        return categoryDao.add(name);
    }

    public void deleteCategory(int id) { categoryDao.deleteCustom(id); }

    // ====================== RÉGLAGES =====================================
    public String getUserName() { return settingsDao.get("user_name", "Utilisateur"); }
    public void setUserName(String name) { settingsDao.set("user_name", name); }

    public String getCurrency() { return settingsDao.get("currency", "€"); }
    public void setCurrency(String symbol) { settingsDao.set("currency", symbol); }

    /** Réinitialise : supprime transactions + catégories personnalisées. */
    public void resetData() {
        transactionDao.deleteAll();
        categoryDao.deleteAllCustom();
    }

    // ====================== FORMATAGE ====================================
    /** Formate un montant avec séparateur de milliers et symbole de devise. */
    public String formatMoney(double amount) {
        String formatted = String.format(Locale.FRANCE, "%,.2f", amount);
        return formatted + " " + getCurrency();
    }

    // ====================== PETITES CLASSES RÉSULTAT =====================
    public static class MonthStat {
        public final String month;   // ex "2026-03"
        public final double amount;
        public MonthStat(String month, double amount) {
            this.month = month;
            this.amount = amount;
        }
    }

    public static class CategoryStat {
        public final String category;
        public final double amount;
        public CategoryStat(String category, double amount) {
            this.category = category;
            this.amount = amount;
        }
    }
}
