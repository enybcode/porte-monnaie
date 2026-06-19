package com.portemonnaie.dao;

import com.portemonnaie.db.Database;
import com.portemonnaie.model.Transaction;
import com.portemonnaie.model.TransactionType;
import com.portemonnaie.service.TransactionFilter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Accès aux données des transactions (Create / Read / Update / Delete).
 * Toutes les requêtes utilisent des requêtes préparées (PreparedStatement)
 * pour éviter les injections SQL.
 */
public class TransactionDao {

    private final Connection cn = Database.get().getConnection();

    /** Insère une transaction et renseigne son id auto-généré. */
    public void insert(Transaction t) {
        String sql = "INSERT INTO transactions(title, amount, type, category, date, description) "
                   + "VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getTitle());
            ps.setDouble(2, t.getAmount());
            ps.setString(3, t.getType().name());
            ps.setString(4, t.getCategory());
            ps.setString(5, t.getDate().toString());
            ps.setString(6, t.getDescription());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    t.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout de la transaction", e);
        }
    }

    /** Met à jour une transaction existante (identifiée par son id). */
    public void update(Transaction t) {
        String sql = "UPDATE transactions SET title=?, amount=?, type=?, category=?, "
                   + "date=?, description=? WHERE id=?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, t.getTitle());
            ps.setDouble(2, t.getAmount());
            ps.setString(3, t.getType().name());
            ps.setString(4, t.getCategory());
            ps.setString(5, t.getDate().toString());
            ps.setString(6, t.getDescription());
            ps.setInt(7, t.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la modification", e);
        }
    }

    public void delete(int id) {
        try (PreparedStatement ps = cn.prepareStatement("DELETE FROM transactions WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression", e);
        }
    }

    /** Supprime TOUTES les transactions (utilisé par la réinitialisation). */
    public void deleteAll() {
        try (Statement st = cn.createStatement()) {
            st.executeUpdate("DELETE FROM transactions");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la réinitialisation", e);
        }
    }

    /**
     * Recherche les transactions selon un filtre. Le SQL est assemblé
     * dynamiquement mais reste paramétré (pas de concaténation de valeurs).
     */
    public List<Transaction> find(TransactionFilter f) {
        StringBuilder sql = new StringBuilder("SELECT * FROM transactions WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (f != null) {
            if (f.type != null) {
                sql.append(" AND type = ?");
                params.add(f.type.name());
            }
            if (f.category != null && !f.category.isBlank()) {
                sql.append(" AND category = ?");
                params.add(f.category);
            }
            if (f.keyword != null && !f.keyword.isBlank()) {
                sql.append(" AND (title LIKE ? OR description LIKE ?)");
                String like = "%" + f.keyword.trim() + "%";
                params.add(like);
                params.add(like);
            }
            if (f.dateFrom != null) {
                sql.append(" AND date >= ?");
                params.add(f.dateFrom.toString());
            }
            if (f.dateTo != null) {
                sql.append(" AND date <= ?");
                params.add(f.dateTo.toString());
            }
            if (f.amountMin != null) {
                sql.append(" AND amount >= ?");
                params.add(f.amountMin);
            }
            if (f.amountMax != null) {
                sql.append(" AND amount <= ?");
                params.add(f.amountMax);
            }
        }
        sql.append(" ORDER BY date DESC, id DESC");

        List<Transaction> result = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche", e);
        }
        return result;
    }

    /** Renvoie les N transactions les plus récentes. */
    public List<Transaction> findRecent(int limit) {
        List<Transaction> result = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY date DESC, id DESC LIMIT ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du chargement des récentes", e);
        }
        return result;
    }

    /** Convertit une ligne SQL en objet Transaction. */
    private Transaction mapRow(ResultSet rs) throws SQLException {
        return new Transaction(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getDouble("amount"),
            TransactionType.valueOf(rs.getString("type")),
            rs.getString("category"),
            LocalDate.parse(rs.getString("date")),
            rs.getString("description")
        );
    }
}
