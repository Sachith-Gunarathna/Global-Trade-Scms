package com.nexcentauri.scms.security;

import com.nexcentauri.scms.entity.SystemUser;
import com.nexcentauri.scms.entity.Vendor;
import com.nexcentauri.scms.service.AuditService;
import com.nexcentauri.scms.service.AuthService;
import com.nexcentauri.scms.service.VendorService;
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

    @EJB
    private VendorService vendorService;

    @EJB
    private AuditService auditService;

    public SystemUser requireAuthenticated() {
        Principal principal = securityContext.getCallerPrincipal();
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            auditService.logAction("AccessGuard", "requireAuthenticated", "SECURITY", 0L, false, "Authentication required");
            throw new NotAuthorizedException("Authentication is required.");
        }
        SystemUser user = authService.findByEmail(principal.getName());
        if (user == null || !Boolean.TRUE.equals(user.getEnabled())) {
            auditService.logAction("AccessGuard", "requireAuthenticated", "SECURITY", 0L, false, "Authenticated account unavailable");
            throw new NotAuthorizedException("The authenticated user account is unavailable.");
        }
        return user;
    }

    public SystemUser requireAnyRole(String... roles) {
        SystemUser user = requireAuthenticated();
        boolean allowed = Arrays.stream(roles).anyMatch(role -> role.equals(user.getRole()));
        if (!allowed) {
            auditService.logAction("AccessGuard", "requireAnyRole", "SECURITY", 0L, false, "Denied role " + user.getRole());
            throw new ForbiddenException("Your role does not allow this operation.");
        }
        return user;
    }

    public boolean isVendorRepresentative(SystemUser user) {
        return user != null && "VENDOR_REP".equals(user.getRole());
    }

    public Vendor requireRepresentativeVendor(SystemUser user) {
        if (!isVendorRepresentative(user)) throw new ForbiddenException("A vendor representative account is required.");
        Vendor vendor = vendorService.findRepresentativeVendor(user);
        if (vendor == null) {
            auditService.logAction("AccessGuard", "requireRepresentativeVendor", "SECURITY", 0L, false, "Vendor account is not linked to a supplier profile");
            throw new ForbiddenException("This vendor account is not linked to a supplier profile.");
        }
        return vendor;
    }
}
