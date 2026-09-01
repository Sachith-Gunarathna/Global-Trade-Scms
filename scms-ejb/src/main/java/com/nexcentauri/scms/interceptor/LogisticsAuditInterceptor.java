package com.nexcentauri.scms.interceptor;

import com.nexcentauri.scms.interceptor.binding.AuditTrail;
import com.nexcentauri.scms.service.AuditService;
import com.nexcentauri.scms.service.InterceptorExecutionMonitor;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.util.logging.Level;
import java.util.logging.Logger;

@AuditTrail
@Interceptor
@Priority(Interceptor.Priority.APPLICATION + 10)
public class LogisticsAuditInterceptor {

    private static final Logger LOGGER = Logger.getLogger(LogisticsAuditInterceptor.class.getName());

    @Inject
    private AuditService auditService;

    @Inject
    private InterceptorExecutionMonitor executionMonitor;

    @AroundInvoke
    public Object audit(InvocationContext context) throws Exception {
        long started = System.nanoTime();
        String component = context.getMethod().getDeclaringClass().getSimpleName();
        String method = context.getMethod().getName();
        String operation = component + "." + method;

        try {
            Object result = context.proceed();
            long duration = elapsed(started);

            auditService.logAction(component, method, "BUSINESS", duration, true, "completed");
            executionMonitor.record("AUDIT", operation, true, duration, "Audit interceptor completed");

            LOGGER.info(
                    "Audit Interceptor executed: " + operation
                            + " | success=true"
                            + " | durationMs=" + duration
            );

            return result;
        } catch (Exception exception) {
            long duration = elapsed(started);
            String detail = safeMessage(exception);

            auditService.logAction(component, method, "BUSINESS", duration, false, detail);
            executionMonitor.record("AUDIT", operation, false, duration, detail);

            LOGGER.log(
                    Level.WARNING,
                    "Audit Interceptor executed: " + operation
                            + " | success=false"
                            + " | durationMs=" + duration
                            + " | detail=" + detail
            );

            throw exception;
        }
    }

    private long elapsed(long started) {
        return (System.nanoTime() - started) / 1_000_000L;
    }

    private String safeMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName() : message;
    }
}
