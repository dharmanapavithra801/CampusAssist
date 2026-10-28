-- CampusAssist database setup
-- Run once:  mysql -u root -p < sql/schema.sql

CREATE DATABASE IF NOT EXISTS campusassist;
USE campusassist;

CREATE TABLE IF NOT EXISTS complaints (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    student_name  VARCHAR(100) NOT NULL,
    title         VARCHAR(150) NOT NULL,
    description   TEXT         NOT NULL,
    category      VARCHAR(30)  NOT NULL,   -- ACADEMIC, HOSTEL, FEES ...
    priority      VARCHAR(10)  NOT NULL,   -- LOW, MEDIUM, HIGH
    status        VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    classified_by VARCHAR(10)  NOT NULL,   -- AI or RULES (who decided category/priority)
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index because we filter by status often (view open complaints)
CREATE INDEX idx_complaints_status ON complaints(status);
