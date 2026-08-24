package com.nexcentauri.scms.interceptor;

import com.nexcentauri.scms.service.AuditService;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.Interceptors;
import jakarta.interceptor.InvocationContext;
import java.util.logging.Logger;


public class LogisticsAuditInterceptor {

    private static final Logger LOGGER = Logger.getLogger(LogisticsAuditInterceptor.class.getName());

    private static final String SYSTEM_USER = "SYSTEM";
    private static final String ACTION_INVOKED = "INVOKED";
    private static final String ACTION_COMPLETED = "COMPLETED";
    private static final String ACTION_FAILED = "FAILED";

    @Inject
    private AuditService auditService;

    @AroundInvoke
    public Object auditAndMonitor(InvocationContext invocationContext) throws Exception{

        long startTime = System.currentTimeMillis();
        String methodName = invocationContext.getMethod().getName();
        String targetClass = invocationContext.getTarget().getClass().getSimpleName();

        logInvocation(methodName, targetClass);
        persistAuditLog(ACTION_INVOKED, methodName);

        try {
            Object result = invocationContext.proceed();

            long executionTime = System.currentTimeMillis() - startTime;
            logSuccess(methodName, executionTime);
            persistAuditLog(ACTION_COMPLETED, methodName);

            return result;
        }catch (Exception e){
            logFailure(methodName, e);
            persistAuditLog(ACTION_FAILED, methodName);
            throw e;
        }

    }

    private void logInvocation(String methodName, String targetClassName){
        LOGGER.info(() -> "[AUDIT LOG] Operation '" + methodName + "' invoked on " + targetClassName + ".");
    }

    private void logSuccess(String methodName, long executionTimeMillis) {
        LOGGER.info(() -> "[PERFORMANCE] Operation '" + methodName
                + "' completed successfully in " + executionTimeMillis + " ms");
    }

    private void logFailure(String methodName, Exception exception) {
        LOGGER.severe(() -> "[AUDIT ERROR] Operation '" + methodName + "' failed: " + exception.getMessage());
    }

    private void persistAuditLog(String action, String methodName) {
        auditService.logAction(action, methodName, SYSTEM_USER);
    }

}
