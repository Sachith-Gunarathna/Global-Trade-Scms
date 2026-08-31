package com.nexcentauri.scms.security;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.AutoApplySession;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStoreHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RequestScoped
@AutoApplySession
public class ScmsAuthenticationMechanism
        implements HttpAuthenticationMechanism {

    @Inject
    private IdentityStoreHandler identityStoreHandler;

    @Override
    public AuthenticationStatus validateRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpMessageContext context
    ) throws AuthenticationException {

        Credential credential =
                context.getAuthParameters().getCredential();

        if (credential == null) {
            return context.doNothing();
        }

        CredentialValidationResult result =
                identityStoreHandler.validate(credential);

        if (result.getStatus()
                == CredentialValidationResult.Status.VALID) {

            return context.notifyContainerAboutLogin(result);
        }

        return context.responseUnauthorized();
    }
}