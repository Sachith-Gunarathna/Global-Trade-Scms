package com.nexcentauri.scms.interceptor;

import com.nexcentauri.scms.entity.Vendor;
import com.nexcentauri.scms.exception.SupplyChainApplicationException;
import com.nexcentauri.scms.interceptor.binding.VendorValidated;
import com.nexcentauri.scms.service.InterceptorExecutionMonitor;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.util.logging.Level;
import java.util.logging.Logger;

@VendorValidated
@Interceptor
@Priority(Interceptor.Priority.APPLICATION + 5)
public class VendorValidationInterceptor {

    private static final Logger LOGGER = Logger.getLogger(VendorValidationInterceptor.class.getName());

    @Inject
    private InterceptorExecutionMonitor executionMonitor;

    @AroundInvoke
    public Object validate(InvocationContext context) throws Exception {
        long started = System.nanoTime();
        String operation = operationName(context);

        try {
            for (Object parameter : context.getParameters()) {
                if (parameter instanceof Vendor vendor) {
                    validateVendor(vendor);
                }
            }

            Object result = context.proceed();
            record(operation, true, elapsed(started), "Vendor validation passed");
            return result;
        } catch (Exception exception) {
            record(operation, false, elapsed(started), safeMessage(exception));
            throw exception;
        }
    }

    private void record(String operation, boolean success, long duration, String detail) {
        executionMonitor.record("VENDOR_VALIDATION", operation, success, duration, detail);

        LOGGER.log(
                success ? Level.INFO : Level.WARNING,
                "Vendor Validation Interceptor executed: " + operation
                        + " | success=" + success
                        + " | durationMs=" + duration
        );
    }

    private void validateVendor(Vendor vendor) throws SupplyChainApplicationException {
        if (vendor.getName() == null || vendor.getName().isBlank()) {
            throw new SupplyChainApplicationException("Vendor name is required.");
        }
        if (vendor.getEmail() == null || !vendor.getEmail().contains("@")) {
            throw new SupplyChainApplicationException("A valid vendor email is required.");
        }
        if (vendor.getRating() != null && (vendor.getRating() < 1.0 || vendor.getRating() > 5.0)) {
            throw new SupplyChainApplicationException("Vendor rating must be between 1 and 5.");
        }
        if (vendor.getOnTimeRate() != null && (vendor.getOnTimeRate() < 0.0 || vendor.getOnTimeRate() > 100.0)) {
            throw new SupplyChainApplicationException("Vendor on-time rate must be between 0 and 100.");
        }
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
