package com.nexcentauri.scms.security;

import com.nexcentauri.scms.entity.SystemUser;
import com.nexcentauri.scms.service.AuthService;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAuthorizedException;
import java.security.Principal;
import java.util.Arrays;

@RequestScoped
public class AccessGuard {
    @Inject
    private SecurityContext securityContext;

    @EJB
    private AuthService authService;

    public SystemUser requireAuthenticated() {
        Principal principal = securityContext.getCallerPrincipal();
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new NotAuthorizedException("Authentication is required.");
        }
        SystemUser user = authService.findByEmail(principal.getName());
        if (user == null || !Boolean.TRUE.equals(user.getEnabled())) {
            throw new NotAuthorizedException("The authenticated user account is unavailable.");
        }
        return user;
    }

    public SystemUser requireAnyRole(String... roles) {
        SystemUser user = requireAuthenticated();
        boolean allowed = Arrays.stream(roles).anyMatch(role -> role.equals(user.getRole()));
        if (!allowed) {
            throw new ForbiddenException("Your role does not allow this operation.");
        }
        return user;
    }
}
