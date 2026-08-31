package com.nexcentauri.scms.security;

import jakarta.security.enterprise.authentication.mechanism.http.AutoApplySession;
import javax.security.auth.spi.LoginModule;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityArchitectureTest {
    @Test
    void authenticationMechanismUsesContainerManagedSessionPropagation() {
        assertNotNull(ScmsAuthenticationMechanism.class.getAnnotation(AutoApplySession.class));
    }

    @Test
    void accessGuardProvidesVendorIsolationChecks() throws Exception {
        assertNotNull(AccessGuard.class.getDeclaredMethod("requireRepresentativeVendor", com.nexcentauri.scms.entity.SystemUser.class));
        assertTrue(java.util.Arrays.stream(AccessGuard.class.getDeclaredMethods()).anyMatch(method -> "requireAnyRole".equals(method.getName())));
    }

    @Test
    void customJaasLoginModuleIsAvailableAsAuthenticationMode() {
        assertTrue(LoginModule.class.isAssignableFrom(ScmsJaasLoginModule.class));
        ScmsJaasConfiguration configuration = new ScmsJaasConfiguration();
        assertNotNull(configuration.getAppConfigurationEntry("SCMS"));
        assertEquals(ScmsJaasLoginModule.class.getName(), configuration.getAppConfigurationEntry("SCMS")[0].getLoginModuleName());
    }
}
