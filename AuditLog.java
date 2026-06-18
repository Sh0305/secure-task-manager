package com.securetask.taskmanager.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // What happened — CREATE, UPDATE, DELETE
    private String action;

    // What was affected — TASK, USER
    private String entityType;

    // The id of the task or user that was affected
    private Long entityId;

    // Email of the person who did this — from JWT token
    private String performedBy;

    // Human readable description eg "Task 'Fix bug' marked COMPLETED"
    private String details;

    @Column(name = "performed_at")
    private LocalDateTime performedAt;

    @PrePersist
    public void prePersist() {
        this.performedAt = LocalDateTime.now();
    }
}
