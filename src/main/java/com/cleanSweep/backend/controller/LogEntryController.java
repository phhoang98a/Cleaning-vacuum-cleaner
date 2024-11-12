package com.cleanSweep.backend.controller;

import com.cleanSweep.backend.infrastructure.LogEntry;
import com.cleanSweep.backend.infrastructure.LogEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogEntryController {

    @Autowired
    private LogEntryRepository logEntryRepository;

    @GetMapping
    public List<LogEntry> getAllLogs() {
        return logEntryRepository.findAll();
    }

    @PostMapping
    public LogEntry saveLog(@RequestBody String message) {
        LogEntry entry = new LogEntry();
        entry.setMessage(message);
        entry.setTimestamp(java.time.LocalDateTime.now());
        return logEntryRepository.save(entry);
    }
}
