package com.nexcentauri.scms.interceptor;

import com.nexcentauri.scms.entity.CustomsDocument;
import com.nexcentauri.scms.entity.Shipment;
import com.nexcentauri.scms.exception.CustomsComplianceException;
import com.nexcentauri.scms.interceptor.binding.ComplianceChecked;
import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@ComplianceChecked
@Interceptor
@Priority(Interceptor.Priority.APPLICATION + 6)
public class ComplianceInterceptor {
    @AroundInvoke
    public Object validate(InvocationContext context) throws Exception {
        for (Object parameter : context.getParameters()) {
            if (parameter instanceof CustomsDocument document) {
                if (document.getDocumentNumber() == null || document.getDocumentNumber().isBlank()) {
                    throw new CustomsComplianceException("Customs document number is required.");
                }
            }
            if (parameter instanceof Shipment shipment) {
                if (shipment.getOrigin() == null || shipment.getOrigin().isBlank() || shipment.getDestination() == null || shipment.getDestination().isBlank()) {
                    throw new CustomsComplianceException("Shipment origin and destination are required for compliance validation.");
                }
                if (shipment.getOrigin().trim().equalsIgnoreCase(shipment.getDestination().trim())) {
                    throw new CustomsComplianceException("Shipment origin and destination cannot be the same.");
                }
            }
        }
        return context.proceed();
    }
}
