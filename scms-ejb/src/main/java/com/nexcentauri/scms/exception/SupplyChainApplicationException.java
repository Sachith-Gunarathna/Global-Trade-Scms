package com.nexcentauri.scms.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException
public class SupplyChainApplicationException extends RuntimeException{

    public SupplyChainApplicationException(String message){
        super(message);
    }

    public SupplyChainApplicationException(String message, Throwable cause){
        super(message,cause);
    }

}
