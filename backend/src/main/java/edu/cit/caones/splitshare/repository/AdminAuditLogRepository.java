package edu.cit.caones.splitshare.repository;

import edu.cit.caones.splitshare.entity.AdminAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, Long> {
}
