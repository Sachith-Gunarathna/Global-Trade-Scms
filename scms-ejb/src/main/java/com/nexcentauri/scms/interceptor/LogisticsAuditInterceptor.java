package com.nexcentauri.scms.interceptor;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

import java.util.logging.Logger;

public class LogisticsAuditInterceptor {

    private static final Logger logger = Logger.getLogger(LogisticsAuditInterceptor.class.getName());

    @AroundInvoke
    public Object auditAndMonitor(InvocationContext invocationContext) throws Exception{

        String methodName = invocationContext.getMethod().getName();
        logger.info("[AUDIT LOG] Initiating logistics operation: "+methodName);

        long startTime = System.currentTimeMillis();

        try {
            Object result = invocationContext.proceed();

            long executionTime = System.currentTimeMillis() - startTime;
            logger.info("[PERFORMANCE] Operation '"+ methodName +"' completed successfully in "+executionTime+" ms");

            return result;
        }catch (Exception e){
            logger.severe("[AUDIT ERROR] Operation '"+ methodName +"' failed: "+e.getMessage());
            throw e;
        }

    }

}
