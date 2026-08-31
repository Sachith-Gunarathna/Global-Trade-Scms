package com.nexcentauri.scms.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.security.enterprise.authentication.mechanism.http.AutoApplySession;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStoreHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@AutoApplySession
@ApplicationScoped
public class ScmsAuthenticationMechanism implements HttpAuthenticationMechanism {
    @Inject
    private IdentityStoreHandler identityStoreHandler;

    @Override
    public AuthenticationStatus validateRequest(HttpServletRequest request, HttpServletResponse response, HttpMessageContext context) {
        AuthenticationParameters parameters = context.getAuthParameters();
        Credential credential = parameters == null ? null : parameters.getCredential();
        if (credential != null) {
            CredentialValidationResult result = identityStoreHandler.validate(credential);
            if (result.getStatus() == CredentialValidationResult.Status.VALID) {
                request.getSession(true);
                return context.notifyContainerAboutLogin(result);
            }
            return context.responseUnauthorized();
        }
        return context.doNothing();
    }
}
