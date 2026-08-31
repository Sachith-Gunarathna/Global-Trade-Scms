package com.nexcentauri.scms.security;

import com.nexcentauri.scms.entity.SystemUser;
import com.nexcentauri.scms.exception.SupplyChainApplicationException;
import com.nexcentauri.scms.service.AuthService;
import jakarta.annotation.Priority;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;
import java.util.Set;

@ApplicationScoped
@Priority(100)
public class ScmsIdentityStore implements IdentityStore {
    @EJB
    private AuthService authService;

    @Override
    public CredentialValidationResult validate(Credential credential) {
        if (!(credential instanceof UsernamePasswordCredential userCredential)) return CredentialValidationResult.NOT_VALIDATED_RESULT;
        try {
            String password = new String(userCredential.getPassword().getValue());
            SystemUser user = authService.authenticate(userCredential.getCaller(), password);
            return new CredentialValidationResult(user.getEmail(), Set.of(user.getRole()));
        } catch (SupplyChainApplicationException exception) {
            return CredentialValidationResult.INVALID_RESULT;
        }
    }

    @Override
    public Set<ValidationType> validationTypes() {
        return Set.of(ValidationType.VALIDATE);
    }
}
