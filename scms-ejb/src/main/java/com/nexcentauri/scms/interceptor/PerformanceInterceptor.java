package com.nexcentauri.scms.interceptor;

import com.nexcentauri.scms.interceptor.binding.Monitored;
import com.nexcentauri.scms.service.InterceptorExecutionMonitor;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.util.logging.Level;
import java.util.logging.Logger;

@Monitored
@Interceptor
@Priority(Interceptor.Priority.APPLICATION + 20)
public class PerformanceInterceptor {

    private static final Logger LOGGER = Logger.getLogger(PerformanceInterceptor.class.getName());

    @Inject
    private InterceptorExecutionMonitor executionMonitor;

    @AroundInvoke
    public Object measure(InvocationContext context) throws Exception {
        long started = System.nanoTime();
        boolean success = false;
        String operation = operationName(context);

        try {
            Object result = context.proceed();
            success = true;
            return result;
        } finally {
            long duration = elapsed(started);

            executionMonitor.record(
                    "PERFORMANCE",
                    operation,
                    success,
                    duration,
                    success ? "Method completed" : "Method failed"
            );

            LOGGER.log(
                    success ? Level.INFO : Level.WARNING,
                    "Performance Interceptor executed: " + operation
                            + " | success=" + success
                            + " | durationMs=" + duration
            );
        }
    }

    private String operationName(InvocationContext context) {
        return context.getMethod().getDeclaringClass().getSimpleName()
                + "." + context.getMethod().getName();
    }

    private long elapsed(long started) {
        return (System.nanoTime() - started) / 1_000_000L;
    }
}
