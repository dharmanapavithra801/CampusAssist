package com.campusassist.service;

import com.campusassist.ai.Classification;
import com.campusassist.ai.ComplaintClassifier;
import com.campusassist.dao.ComplaintDAO;
import com.campusassist.exception.InvalidInputException;
import com.campusassist.model.Complaint;
import com.campusassist.model.Status;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * The service layer holds the business rules: validate input, ask the AI, then save.
 * Main (the menu) talks only to this class, and this class talks to the DAO and classifier.
 *
 *   Main  ->  ComplaintService  ->  ComplaintClassifier (AI or rules)
 *                    |
 *                    +---------->  ComplaintDAO  ->  MySQL
 */
public class ComplaintService {

    private final ComplaintDAO dao;
    private final ComplaintClassifier classifier;

    // Dependencies are passed in (constructor injection), so tests can pass fakes.
    public ComplaintService(ComplaintDAO dao, ComplaintClassifier classifier) {
        this.dao = dao;
        this.classifier = classifier;
    }

    public Complaint submit(String studentName, String title, String description) throws SQLException {
        studentName = require(studentName, "Name", 100);
        title = require(title, "Title", 150);
        description = require(description, "Description", 2000);

        Classification result = classifier.classify(title, description);   // the AI step
        Complaint complaint = new Complaint(studentName, title, description,
                result.category(), result.priority(), result.source());
        dao.add(complaint);                                                // the save step
        return complaint;
    }

    public List<Complaint> all() throws SQLException {
        return dao.findAll();
    }

    public List<Complaint> byStatus(Status status) throws SQLException {
        return dao.findByStatus(status);
    }

    public Complaint get(int id) throws SQLException {
        return dao.findById(id)
                .orElseThrow(() -> new InvalidInputException("No complaint with id " + id));
    }

    public void changeStatus(int id, Status status) throws SQLException {
        if (!dao.updateStatus(id, status)) {
            throw new InvalidInputException("No complaint with id " + id);
        }
    }

    public Map<String, Integer> categoryReport() throws SQLException {
        return dao.countByCategory();
    }

    public Map<String, Integer> statusReport() throws SQLException {
        return dao.countByStatus();
    }

    private String require(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new InvalidInputException(field + " cannot be empty");
        }
        value = value.trim();
        if (value.length() > maxLength) {
            throw new InvalidInputException(field + " is too long (max " + maxLength + " characters)");
        }
        return value;
    }
}
