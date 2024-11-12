package com.cleanSweep.backend.infrastructure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LogRepoImpl {

    @Autowired
    private LogEntryRepository logEntryRepository;

    public void saveLog(String message) {
        LogEntry entry = new LogEntry();
        entry.setTimestamp(LocalDateTime.now());
        entry.setMessage(message);
        logEntryRepository.save(entry);
    }

    public List<LogEntry> getAllLogs() {
        return logEntryRepository.findAll();
    }
}
