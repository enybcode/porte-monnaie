package com.portemonnaie;

import com.portemonnaie.model.Goal;
import com.portemonnaie.model.Transaction;
import com.portemonnaie.model.TransactionType;
import com.portemonnaie.service.AuthService;
import com.portemonnaie.service.WalletService;

import java.time.LocalDate;

public class SmokeTest {
    public static void main(String[] args) {
        AuthService auth = new AuthService();
        check(auth.register("Enzo", "Test", "enzo@test.local", "1234"), "creation compte");
        check(auth.login("enzo@test.local", "1234"), "connexion compte");
        check(!auth.login("enzo@test.local", "mauvais"), "refus mauvais mot de passe");

        WalletService wallet = new WalletService();
        wallet.addTransaction(new Transaction("Salaire", 1200, TransactionType.REVENU, "Salaire", LocalDate.now(), ""));
        wallet.addTransaction(new Transaction("Courses", 75.50, TransactionType.DEPENSE, "Nourriture", LocalDate.now(), ""));
        check(close(wallet.getTotalIncome(), 1200), "total revenus");
        check(close(wallet.getTotalExpense(), 75.50), "total depenses");
        check(close(wallet.getBalance(), 1124.50), "solde");

        check(wallet.addCategory("Projet BTS"), "ajout categorie");
        wallet.addGoal(new Goal("Acheter un PC", 900, 250));
        check(close(wallet.getSavingsTotal(), 250), "total epargne");
        check(!wallet.getGoals().isEmpty(), "liste objectifs");

        wallet.resetData();
        check(wallet.getAllTransactions().isEmpty(), "reset transactions");
        check(wallet.getGoals().isEmpty(), "reset objectifs");
        System.out.println("SMOKE_TEST_OK");
    }

    private static boolean close(double actual, double expected) {
        return Math.abs(actual - expected) < 0.001;
    }

    private static void check(boolean condition, String label) {
        if (!condition) {
            throw new IllegalStateException("Echec test: " + label);
        }
    }
}
