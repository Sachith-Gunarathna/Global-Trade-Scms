package com.nexcentauri.scms.service;

import com.nexcentauri.scms.exception.SupplyChainApplicationException;
import com.nexcentauri.scms.interceptor.ComplianceInterceptor;
import com.nexcentauri.scms.interceptor.LogisticsAuditInterceptor;
import com.nexcentauri.scms.interceptor.PerformanceInterceptor;
import com.nexcentauri.scms.interceptor.VendorValidationInterceptor;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.ApplicationException;
import jakarta.ejb.Local;
import jakarta.ejb.Schedule;
import jakarta.ejb.Timeout;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdvancedEjbArchitectureTest {
    @Test
    void timerServiceContainsPersistentDeclarativeAndProgrammaticTimers() throws Exception {
        assertPersistentSchedule("monitorShipments");
        assertPersistentSchedule("monitorInventory");
        assertPersistentSchedule("evaluateVendorPerformance");
        assertPersistentSchedule("optimizeRoutes");
        Method timeout = LogisticsTimerService.class.getDeclaredMethod("handleProgrammaticTimer", jakarta.ejb.Timer.class);
        assertNotNull(timeout.getAnnotation(Timeout.class));
    }

    @Test
    void interceptorFrameworkCoversRequiredConcerns() {
        assertInterceptor(LogisticsAuditInterceptor.class);
        assertInterceptor(PerformanceInterceptor.class);
        assertInterceptor(VendorValidationInterceptor.class);
        assertInterceptor(ComplianceInterceptor.class);
    }

    @Test
    void transactionDemarcationCoversBeanAndContainerManagedStrategies() throws Exception {
        TransactionManagement management = CustomsTransactionService.class.getAnnotation(TransactionManagement.class);
        assertNotNull(management);
        assertEquals(TransactionManagementType.BEAN, management.value());
        Method reserveStock = InventoryService.class.getDeclaredMethod("reserveStock", Long.class, int.class);
        TransactionAttribute reserveAttribute = reserveStock.getAnnotation(TransactionAttribute.class);
        assertNotNull(reserveAttribute);
        assertEquals(TransactionAttributeType.MANDATORY, reserveAttribute.value());
        Method raiseAlert = AlertService.class.getDeclaredMethod("raise", String.class, String.class, String.class, String.class, String.class);
        TransactionAttribute alertAttribute = raiseAlert.getAnnotation(TransactionAttribute.class);
        assertNotNull(alertAttribute);
        assertEquals(TransactionAttributeType.REQUIRES_NEW, alertAttribute.value());
    }

    @Test
    void declarativeSecurityAndRollbackApplicationExceptionsAreConfigured() throws Exception {
        Method adminMethod = DeclarativeAuthorizationService.class.getDeclaredMethod("administrativeOperation");
        RolesAllowed rolesAllowed = adminMethod.getAnnotation(RolesAllowed.class);
        assertNotNull(rolesAllowed);
        assertEquals("ADMIN", rolesAllowed.value()[0]);
        ApplicationException applicationException = SupplyChainApplicationException.class.getAnnotation(ApplicationException.class);
        assertNotNull(applicationException);
        assertTrue(applicationException.rollback());
    }

    @Test
    void carrierRecoveryUsesLocalGatewayAbstraction() {
        assertTrue(CarrierGateway.class.isAssignableFrom(CarrierIntegrationService.class));
        assertNotNull(CarrierGateway.class.getAnnotation(Local.class));
    }

    @Test
    void vendorScopedQueriesAndDisruptionRecoveryAreAvailable() throws Exception {
        assertNotNull(ShipmentService.class.getDeclaredMethod("getAllForVendor", Long.class));
        assertNotNull(InventoryService.class.getDeclaredMethod("getAllForVendor", Long.class));
        assertNotNull(OrderService.class.getDeclaredMethod("getAllForVendor", Long.class));
        assertNotNull(CustomsService.class.getDeclaredMethod("getAllForVendor", Long.class));
        assertNotNull(DisruptionRecoveryService.class.getDeclaredMethod("reportWeatherDisruption", Long.class, String.class));
    }

    private void assertPersistentSchedule(String methodName) throws Exception {
        Method method = LogisticsTimerService.class.getDeclaredMethod(methodName);
        Schedule schedule = method.getAnnotation(Schedule.class);
        assertNotNull(schedule);
        assertTrue(schedule.persistent());
    }

    private void assertInterceptor(Class<?> type) {
        assertNotNull(type.getAnnotation(Interceptor.class));
        boolean hasAroundInvoke = false;
        for (Method method : type.getDeclaredMethods()) {
            if (method.getAnnotation(AroundInvoke.class) != null) {
                hasAroundInvoke = true;
                break;
            }
        }
        assertTrue(hasAroundInvoke);
    }
}
