package com.nexcentauri.scms.interceptor;

import com.nexcentauri.scms.entity.CustomsDocument;
import com.nexcentauri.scms.entity.Shipment;
import com.nexcentauri.scms.exception.CustomsComplianceException;
import com.nexcentauri.scms.interceptor.binding.ComplianceChecked;
import com.nexcentauri.scms.service.InterceptorExecutionMonitor;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.util.logging.Level;
import java.util.logging.Logger;

@ComplianceChecked
@Interceptor
@Priority(Interceptor.Priority.APPLICATION + 6)
public class ComplianceInterceptor {

    private static final Logger LOGGER = Logger.getLogger(ComplianceInterceptor.class.getName());

    @Inject
    private InterceptorExecutionMonitor executionMonitor;

    @AroundInvoke
    public Object validate(InvocationContext context) throws Exception {
        long started = System.nanoTime();
        String operation = operationName(context);

        try {
            for (Object parameter : context.getParameters()) {
                if (parameter instanceof CustomsDocument document) {
                    if (document.getDocumentNumber() == null || document.getDocumentNumber().isBlank()) {
                        throw new CustomsComplianceException("Customs document number is required.");
                    }
                }

                if (parameter instanceof Shipment shipment) {
                    if (shipment.getOrigin() == null || shipment.getOrigin().isBlank()
                            || shipment.getDestination() == null || shipment.getDestination().isBlank()) {
                        throw new CustomsComplianceException(
                                "Shipment origin and destination are required for compliance validation."
                        );
                    }

                    if (shipment.getOrigin().trim().equalsIgnoreCase(shipment.getDestination().trim())) {
                        throw new CustomsComplianceException(
                                "Shipment origin and destination cannot be the same."
                        );
                    }
                }
            }

            Object result = context.proceed();
            record(operation, true, elapsed(started), "Compliance validation passed");
            return result;
        } catch (Exception exception) {
            record(operation, false, elapsed(started), safeMessage(exception));
            throw exception;
        }
    }

    private void record(String operation, boolean success, long duration, String detail) {
        executionMonitor.record("COMPLIANCE", operation, success, duration, detail);

        LOGGER.log(
                success ? Level.INFO : Level.WARNING,
                "Compliance Interceptor executed: " + operation
                        + " | success=" + success
                        + " | durationMs=" + duration
        );
    }

    private String operationName(InvocationContext context) {
        return context.getMethod().getDeclaringClass().getSimpleName()
                + "." + context.getMethod().getName();
    }

    private long elapsed(long started) {
        return (System.nanoTime() - started) / 1_000_000L;
    }

    private String safeMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName() : message;
    }
}
