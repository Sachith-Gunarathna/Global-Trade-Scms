package com.nexcentauri.scms.interceptor;

import com.nexcentauri.scms.interceptor.binding.Monitored;
import com.nexcentauri.scms.service.PerformanceService;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@Monitored
@Interceptor
@Priority(Interceptor.Priority.APPLICATION + 20)
public class PerformanceInterceptor {
    @Inject
    private PerformanceService performanceService;

    @AroundInvoke
    public Object measure(InvocationContext context) throws Exception {
        long started = System.nanoTime();
        boolean success = false;
        try {
            Object result = context.proceed();
            success = true;
            return result;
        } finally {
            long duration = (System.nanoTime() - started) / 1_000_000L;
            String operation = context.getTarget().getClass().getSimpleName() + "." + context.getMethod().getName();
            performanceService.record("EJB_METHOD", operation, duration, success);
        }
    }
}
