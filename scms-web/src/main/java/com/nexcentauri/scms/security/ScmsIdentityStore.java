package com.nexcentauri.scms.security;

import com.nexcentauri.scms.entity.SystemUser;
import com.nexcentauri.scms.exception.SupplyChainApplicationException;
import com.nexcentauri.scms.service.AuthService;
import jakarta.annotation.Priority;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;
import java.util.Locale;
import java.util.Set;

@ApplicationScoped
@Priority(100)
public class ScmsIdentityStore implements IdentityStore {
    @EJB
    private AuthService authService;

    @Inject
    private ScmsJaasAuthenticator jaasAuthenticator;

    @Override
    public CredentialValidationResult validate(Credential credential) {
        if (!(credential instanceof UsernamePasswordCredential userCredential)) return CredentialValidationResult.NOT_VALIDATED_RESULT;
        if ("JAAS".equals(authenticationMode())) {
            return jaasAuthenticator.authenticate(userCredential.getCaller(), userCredential.getPassword().getValue())
                    .map(identity -> new CredentialValidationResult(identity.username(), Set.of(identity.role())))
                    .orElse(CredentialValidationResult.INVALID_RESULT);
        }
        try {
            String password = new String(userCredential.getPassword().getValue());
            SystemUser user = authService.authenticate(userCredential.getCaller(), password);
            return new CredentialValidationResult(user.getEmail(), Set.of(user.getRole()));
        } catch (SupplyChainApplicationException exception) {
            return CredentialValidationResult.INVALID_RESULT;
        }
    }

    public String authenticationMode() {
        String value = System.getenv("SCMS_AUTH_MODE");
        if (value == null || value.isBlank()) value = System.getProperty("scms.auth.mode", "JAKARTA");
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return "JAAS".equals(normalized) ? "JAAS" : "JAKARTA";
    }

    @Override
    public Set<ValidationType> validationTypes() {
        return Set.of(ValidationType.VALIDATE);
    }
}
