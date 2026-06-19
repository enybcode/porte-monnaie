package com.portemonnaie.dao;

import com.portemonnaie.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Lecture/écriture des réglages (clé/valeur) : nom utilisateur, devise... */
public class SettingsDao {

    private final Connection cn = Database.get().getConnection();

    public String get(String key, String defaultValue) {
        String sql = "SELECT value FROM settings WHERE key=?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("value");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de lecture des réglages", e);
        }
        return defaultValue;
    }

    public void set(String key, String value) {
        // "UPSERT" SQLite : insère ou met à jour si la clé existe déjà
        String sql = "INSERT INTO settings(key, value) VALUES(?, ?) "
                   + "ON CONFLICT(key) DO UPDATE SET value=excluded.value";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur d'écriture des réglages", e);
        }
    }
}
