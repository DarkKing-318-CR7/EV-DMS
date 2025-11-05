package com.uth.ev_dms.audit;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;

@Service
public class AuditService {
    private final List<AuditLog> logs = new ArrayList<>();

    public void record(String username, String action) {
        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setAction(action);
        logs.add(log);
    }

    public List<AuditLog> getAll() {
        return logs;
    }
}
