package com.nexcentauri.scms.service;

import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Singleton
@Startup
@Lock(LockType.READ)
public class TimerExecutionMonitor {

    private static final int MAX_RECORDS = 100;

    private final Deque<TimerExecution> records = new ArrayDeque<>();

    @Lock(LockType.WRITE)
    public void record(String timerName, boolean success, long durationMs) {

        if (records.size() >= MAX_RECORDS) {
            records.removeFirst();
        }

        records.addLast(
                new TimerExecution(
                        timerName,
                        success,
                        durationMs,
                        LocalDateTime.now()
                )
        );
    }

    public List<Map<String, Object>> recent() {

        List<Map<String, Object>> result = new ArrayList<>();
        Iterator<TimerExecution> iterator = records.descendingIterator();

        while (iterator.hasNext()) {

            TimerExecution record = iterator.next();

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("timerName", record.timerName);
            item.put("success", record.success);
            item.put("durationMs", record.durationMs);
            item.put("executedAt", record.executedAt.toString());

            result.add(item);
        }

        return result;
    }

    private static class TimerExecution {

        private final String timerName;
        private final boolean success;
        private final long durationMs;
        private final LocalDateTime executedAt;

        private TimerExecution(
                String timerName,
                boolean success,
                long durationMs,
                LocalDateTime executedAt
        ) {
            this.timerName = timerName;
            this.success = success;
            this.durationMs = durationMs;
            this.executedAt = executedAt;
        }
    }
}