package edu.cit.caones.splitshare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "admin_audit_logs")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String action;

    @Column(nullable = false)
    private String actorEmail;

    @Column(nullable = true)
    private Long targetUserId;

    @Column(nullable = true)
    private String targetUserEmail;

    @Column(nullable = true, length = 1000)
    private String details;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
