package com.auditflow.repo;

import com.auditflow.model.Audit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<Audit, Long>
{
}