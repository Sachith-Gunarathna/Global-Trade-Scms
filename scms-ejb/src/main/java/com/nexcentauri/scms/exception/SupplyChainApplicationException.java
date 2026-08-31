package com.nexcentauri.scms.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class SupplyChainApplicationException extends Exception {
    public SupplyChainApplicationException(String message) { super(message); }
    public SupplyChainApplicationException(String message, Throwable cause) { super(message, cause); }
}
