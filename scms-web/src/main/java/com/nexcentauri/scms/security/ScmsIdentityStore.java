package com.nexcentauri.scms.security;

import com.nexcentauri.scms.entity.SystemUser;
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
@Priority(90)
public class ScmsIdentityStore implements IdentityStore {

    @EJB
    private AuthService authService;

    @Override
    public CredentialValidationResult validate(Credential credential) {

        if (!(credential instanceof UsernamePasswordCredential usernamePassword)) {
            return CredentialValidationResult.NOT_VALIDATED_RESULT;
        }

        try {

            SystemUser user = authService.authenticate(
                    usernamePassword.getCaller(),
                    usernamePassword.getPasswordAsString()
            );

            if (user == null) {
                return CredentialValidationResult.INVALID_RESULT;
            }

            return new CredentialValidationResult(
                    user.getEmail(),
                    Set.of(user.getRole())
            );

        } catch (Exception e) {

            return CredentialValidationResult.INVALID_RESULT;
        }
    }
}