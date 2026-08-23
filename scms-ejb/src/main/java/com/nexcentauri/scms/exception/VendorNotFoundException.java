package com.nexcentauri.scms.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class VendorNotFoundException extends Exception{

    public VendorNotFoundException(String message){
        super(message);
    }

}
