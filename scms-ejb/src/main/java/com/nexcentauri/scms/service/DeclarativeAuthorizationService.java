package com.nexcentauri.scms.service;

import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;

@Stateless
@DeclareRoles({"ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT", "VENDOR_REP"})
public class DeclarativeAuthorizationService {
    @RolesAllowed("ADMIN")
    public String administrativeOperation() {
        return "ADMIN";
    }

    @RolesAllowed({"ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT", "VENDOR_REP"})
    public String authenticatedOperation() {
        return "AUTHENTICATED";
    }
}
