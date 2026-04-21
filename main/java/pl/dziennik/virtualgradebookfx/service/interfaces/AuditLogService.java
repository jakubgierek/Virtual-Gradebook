package pl.dziennik.virtualgradebookfx.service.interfaces;

public interface AuditLogService {
    void logEvent(String username, String eventType, String description);
    void shutdown();
}