package com.nexcentauri.scms.service;

import jakarta.annotation.Resource;
import jakarta.annotation.security.DeclareRoles;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;

@Stateless
@DeclareRoles({"ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT", "VENDOR_REP"})
public class ProgrammaticAuthorizationService {
    @Resource
    private SessionContext sessionContext;

    public boolean canMakeCustomsDecision() {
        return sessionContext.isCallerInRole("ADMIN") || sessionContext.isCallerInRole("CUSTOMS_AGENT");
    }

    public boolean canManageShipments() {
        return sessionContext.isCallerInRole("ADMIN") || sessionContext.isCallerInRole("LOGISTICS_COORDINATOR");
    }
}
