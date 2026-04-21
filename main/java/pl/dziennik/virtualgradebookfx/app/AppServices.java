package pl.dziennik.virtualgradebookfx.app;

import pl.dziennik.virtualgradebookfx.service.impl.FileAuditLogService;
import pl.dziennik.virtualgradebookfx.service.interfaces.AuditLogService;

public class AppServices {
    private static final AuditLogService auditLogService = new FileAuditLogService();

    private AppServices() {
    }

    public static AuditLogService getAuditLogService() {
        return auditLogService;
    }
}