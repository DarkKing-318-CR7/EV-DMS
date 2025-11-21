package com.uth.ev_dms.audit;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {
    private final AuditRepository repo;
    public AuditService(AuditRepository repo) { this.repo = repo; }

    public AuditLog record(String username, String action) {
        AuditLog l = new AuditLog();
        l.setUsername(username);
        l.setAction(action);
        l.setTimestamp(LocalDateTime.now());
        return repo.save(l);
    }

    public List<AuditLog> findAll() {
        return repo.findAll();
    }
}
