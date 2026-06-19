package com.portemonnaie.dao;

import com.portemonnaie.db.Database;
import com.portemonnaie.model.Goal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GoalDao {
    private final Connection cn = Database.get().getConnection();

    public void insert(Goal goal) {
        String sql = "INSERT INTO goals(title, target_amount, current_amount, created_at) VALUES(?,?,?,?)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, goal.getTitle());
            ps.setDouble(2, goal.getTargetAmount());
            ps.setDouble(3, goal.getCurrentAmount());
            ps.setString(4, goal.getCreatedAt().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) goal.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout de l'objectif", e);
        }
    }

    public void update(Goal goal) {
        String sql = "UPDATE goals SET title=?, target_amount=?, current_amount=? WHERE id=?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, goal.getTitle());
            ps.setDouble(2, goal.getTargetAmount());
            ps.setDouble(3, goal.getCurrentAmount());
            ps.setInt(4, goal.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la modification de l'objectif", e);
        }
    }

    public void delete(int id) {
        try (PreparedStatement ps = cn.prepareStatement("DELETE FROM goals WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de l'objectif", e);
        }
    }

    public List<Goal> findAll() {
        List<Goal> goals = new ArrayList<>();
        String sql = "SELECT * FROM goals ORDER BY created_at DESC, id DESC";
        try (Statement st = cn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                goals.add(new Goal(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getDouble("target_amount"),
                        rs.getDouble("current_amount"),
                        LocalDate.parse(rs.getString("created_at"))));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de lecture des objectifs", e);
        }
        return goals;
    }

    public double totalSaved() {
        String sql = "SELECT COALESCE(SUM(current_amount), 0) AS total FROM goals";
        try (Statement st = cn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getDouble("total") : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de calcul de l'épargne", e);
        }
    }

    public void deleteAll() {
        try (Statement st = cn.createStatement()) {
            st.executeUpdate("DELETE FROM goals");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de réinitialisation des objectifs", e);
        }
    }
}
