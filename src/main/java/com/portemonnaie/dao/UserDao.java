package com.portemonnaie.dao;

import com.portemonnaie.db.Database;
import com.portemonnaie.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class UserDao {
    private final Connection cn = Database.get().getConnection();

    public boolean create(String firstName, String lastName, String email, String passwordHash, String salt) {
        String sql = "INSERT INTO users(first_name, last_name, email, password_hash, salt, created_at) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, firstName.trim());
            ps.setString(2, lastName.trim());
            ps.setString(3, email.trim().toLowerCase());
            ps.setString(4, passwordHash);
            ps.setString(5, salt);
            ps.setString(6, LocalDate.now().toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unique")) {
                return false;
            }
            throw new RuntimeException("Erreur lors de la création du compte", e);
        }
    }

    public Credentials findCredentials(String email) {
        String sql = "SELECT id, first_name, last_name, email, password_hash, salt, created_at FROM users WHERE email=?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        LocalDate.parse(rs.getString("created_at")));
                return new Credentials(user, rs.getString("password_hash"), rs.getString("salt"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de connexion", e);
        }
    }

    public static class Credentials {
        public final User user;
        public final String passwordHash;
        public final String salt;

        public Credentials(User user, String passwordHash, String salt) {
            this.user = user;
            this.passwordHash = passwordHash;
            this.salt = salt;
        }
    }
}
