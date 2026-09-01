package com.nexcentauri.scms.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class InterceptorExecutionMonitor {

    private static final int MAX_RECORDS = 200;

    private final Deque<ExecutionRecord> records = new ArrayDeque<>();
    private long sequence = 0L;

    public synchronized void record(
            String interceptor,
            String operation,
            boolean success,
            long durationMs,
            String detail
    ) {
        if (records.size() >= MAX_RECORDS) {
            records.removeFirst();
        }

        records.addLast(new ExecutionRecord(
                ++sequence,
                interceptor,
                operation,
                success,
                durationMs,
                detail,
                LocalDateTime.now()
        ));
    }

    public synchronized List<Map<String, Object>> recent() {
        List<Map<String, Object>> result = new ArrayList<>();
        Iterator<ExecutionRecord> iterator = records.descendingIterator();

        while (iterator.hasNext()) {
            result.add(toMap(iterator.next()));
        }

        return result;
    }

    public synchronized double averagePerformanceDuration() {
        long total = 0L;
        int count = 0;

        for (ExecutionRecord record : records) {
            if ("PERFORMANCE".equals(record.interceptor)) {
                total += record.durationMs;
                count++;
            }
        }

        return count == 0 ? 0.0 : (double) total / count;
    }

    private Map<String, Object> toMap(ExecutionRecord record) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", record.id);
        item.put("type", record.interceptor);
        item.put("interceptor", record.interceptor);
        item.put("operation", record.operation);
        item.put("durationMs", record.durationMs);
        item.put("success", record.success);
        item.put("detail", record.detail);
        item.put("recordedAt", record.executedAt.toString());
        item.put("executedAt", record.executedAt.toString());
        return item;
    }

    private static class ExecutionRecord {
        private final long id;
        private final String interceptor;
        private final String operation;
        private final boolean success;
        private final long durationMs;
        private final String detail;
        private final LocalDateTime executedAt;

        private ExecutionRecord(
                long id,
                String interceptor,
                String operation,
                boolean success,
                long durationMs,
                String detail,
                LocalDateTime executedAt
        ) {
            this.id = id;
            this.interceptor = interceptor;
            this.operation = operation;
            this.success = success;
            this.durationMs = durationMs;
            this.detail = detail;
            this.executedAt = executedAt;
        }
    }
}
