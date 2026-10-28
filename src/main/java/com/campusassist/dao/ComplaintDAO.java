package com.campusassist.dao;

import com.campusassist.model.Category;
import com.campusassist.model.Complaint;
import com.campusassist.model.Priority;
import com.campusassist.model.Status;
import com.campusassist.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * DAO = Data Access Object. This is the ONLY class that knows SQL.
 * If we ever switch from MySQL to another database, only this file changes.
 *
 * Every query uses PreparedStatement with ? placeholders, which stops SQL injection.
 * Every connection is opened in try-with-resources, so it is closed even when an error happens.
 */
public class ComplaintDAO {

    /** Inserts a complaint and returns the id MySQL generated for it. */
    public int add(Complaint c) throws SQLException {
        String sql = "INSERT INTO complaints "
                + "(student_name, title, description, category, priority, status, classified_by) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getStudentName());
            ps.setString(2, c.getTitle());
            ps.setString(3, c.getDescription());
            ps.setString(4, c.getCategory().name());
            ps.setString(5, c.getPriority().name());
            ps.setString(6, c.getStatus().name());
            ps.setString(7, c.getClassifiedBy());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    c.setId(id);
                    return id;
                }
            }
            throw new SQLException("Insert worked but no id was returned");
        }
    }

    public List<Complaint> findAll() throws SQLException {
        return query("SELECT * FROM complaints ORDER BY "
                + "FIELD(priority,'HIGH','MEDIUM','LOW'), created_at DESC", null);
    }

    public List<Complaint> findByStatus(Status status) throws SQLException {
        return query("SELECT * FROM complaints WHERE status = ? ORDER BY created_at DESC", status.name());
    }

    public Optional<Complaint> findById(int id) throws SQLException {
        String sql = "SELECT * FROM complaints WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    /** Returns true if a row was changed, false if no complaint has that id. */
    public boolean updateStatus(int id, Status status) throws SQLException {
        String sql = "UPDATE complaints SET status = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    /** SQL GROUP BY report: how many complaints per category. */
    public Map<String, Integer> countByCategory() throws SQLException {
        return counts("SELECT category AS label, COUNT(*) AS total FROM complaints GROUP BY category");
    }

    public Map<String, Integer> countByStatus() throws SQLException {
        return counts("SELECT status AS label, COUNT(*) AS total FROM complaints GROUP BY status");
    }

    // ---------- private helpers (remove repeated code) ----------

    private List<Complaint> query(String sql, String param) throws SQLException {
        List<Complaint> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (param != null) {
                ps.setString(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    private Map<String, Integer> counts(String sql) throws SQLException {
        Map<String, Integer> result = new LinkedHashMap<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getString("label"), rs.getInt("total"));
            }
        }
        return result;
    }

    /** Turns one database row into one Complaint object. */
    private Complaint map(ResultSet rs) throws SQLException {
        return new Complaint(
                rs.getInt("id"),
                rs.getString("student_name"),
                rs.getString("title"),
                rs.getString("description"),
                Category.valueOf(rs.getString("category")),
                Priority.valueOf(rs.getString("priority")),
                Status.valueOf(rs.getString("status")),
                rs.getString("classified_by"),
                rs.getTimestamp("created_at").toLocalDateTime());
    }
}
