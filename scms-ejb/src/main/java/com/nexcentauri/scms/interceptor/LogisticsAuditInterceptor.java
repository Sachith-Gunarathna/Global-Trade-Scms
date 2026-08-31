package com.nexcentauri.scms.interceptor;

import com.nexcentauri.scms.interceptor.binding.AuditTrail;
import com.nexcentauri.scms.service.AuditService;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@AuditTrail
@Interceptor
@Priority(Interceptor.Priority.APPLICATION + 10)
public class LogisticsAuditInterceptor {
    @Inject
    private AuditService auditService;

    @AroundInvoke
    public Object audit(InvocationContext context) throws Exception {
        long started = System.nanoTime();
        String component = context.getTarget().getClass().getSimpleName();
        String method = context.getMethod().getName();
        try {
            Object result = context.proceed();
            auditService.logAction(component, method, "BUSINESS", elapsed(started), true, "completed");
            return result;
        } catch (Exception exception) {
            auditService.logAction(component, method, "BUSINESS", elapsed(started), false, safeMessage(exception));
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
