package pl.dziennik.virtualgradebookfx.service.impl;

import pl.dziennik.virtualgradebookfx.service.interfaces.AuditLogService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileAuditLogService implements AuditLogService {

    private static final String LOGS_DIRECTORY = "reports";
    private static final String LOG_FILE = "reports/system_log.txt";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void logEvent(String username, String eventType, String description) {
        executorService.submit(() -> {
            try {
                File dir = new File(LOGS_DIRECTORY);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String timestamp = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
                    writer.println(timestamp + " | " + username + " | " + eventType + " | " + description);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
    }
}