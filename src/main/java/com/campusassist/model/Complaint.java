package com.campusassist.model;

import java.time.LocalDateTime;

/**
 * One row of the complaints table, as a Java object.
 * This is the "model" layer: it only holds data, it does no database or AI work.
 */
public class Complaint {
    private int id;                       // 0 until the database gives us one
    private final String studentName;
    private final String title;
    private final String description;
    private final Category category;
    private final Priority priority;
    private Status status;
    private final String classifiedBy;    // "AI" or "RULES"
    private LocalDateTime createdAt;

    public Complaint(String studentName, String title, String description,
                     Category category, Priority priority, String classifiedBy) {
        this.studentName = studentName;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.status = Status.OPEN;
        this.classifiedBy = classifiedBy;
    }

    /** Used by the DAO when rebuilding an object from a database row. */
    public Complaint(int id, String studentName, String title, String description,
                     Category category, Priority priority, Status status,
                     String classifiedBy, LocalDateTime createdAt) {
        this(studentName, title, description, category, priority, classifiedBy);
        this.id = id;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getStudentName() { return studentName; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public Priority getPriority() { return priority; }
    public Status getStatus() { return status; }
    public String getClassifiedBy() { return classifiedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return String.format("#%-3d | %-14s | %-6s | %-11s | %s (by %s, classified by %s)",
                id, category, priority, status, title, studentName, classifiedBy);
    }
}
