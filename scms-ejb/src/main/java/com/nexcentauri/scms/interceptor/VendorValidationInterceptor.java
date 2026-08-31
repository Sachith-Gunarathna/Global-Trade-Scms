package com.nexcentauri.scms.interceptor;

import com.nexcentauri.scms.entity.Vendor;
import com.nexcentauri.scms.exception.SupplyChainApplicationException;
import com.nexcentauri.scms.interceptor.binding.VendorValidated;
import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@VendorValidated
@Interceptor
@Priority(Interceptor.Priority.APPLICATION + 5)
public class VendorValidationInterceptor {
    @AroundInvoke
    public Object validate(InvocationContext context) throws Exception {
        for (Object parameter : context.getParameters()) {
            if (parameter instanceof Vendor vendor) {
                validateVendor(vendor);
            }
        }
        return context.proceed();
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
}
