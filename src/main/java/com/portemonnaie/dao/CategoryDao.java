package com.portemonnaie.dao;

import com.portemonnaie.db.Database;
import com.portemonnaie.model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Accès aux données des catégories. */
public class CategoryDao {

    private final Connection cn = Database.get().getConnection();

    public List<Category> findAll() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY is_default DESC, name ASC";
        try (Statement st = cn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Category(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("is_default") == 1
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de lecture des catégories", e);
        }
        return list;
    }

    /** Ajoute une catégorie personnalisée. Renvoie false si le nom existe déjà. */
    public boolean add(String name) {
        String sql = "INSERT OR IGNORE INTO categories(name, is_default) VALUES(?, 0)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur d'ajout de catégorie", e);
        }
    }

    /** Supprime une catégorie personnalisée (les catégories par défaut sont protégées). */
    public void deleteCustom(int id) {
        String sql = "DELETE FROM categories WHERE id=? AND is_default=0";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de suppression de catégorie", e);
        }
    }

    /** Supprime toutes les catégories personnalisées (pour la réinitialisation). */
    public void deleteAllCustom() {
        try (Statement st = cn.createStatement()) {
            st.executeUpdate("DELETE FROM categories WHERE is_default=0");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de réinitialisation des catégories", e);
        }
    }
}
