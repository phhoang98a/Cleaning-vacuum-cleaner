package com.cleanSweep.backend.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {

}
