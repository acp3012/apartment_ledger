package com.yesh.apartmentledger.exception;

public class UnAuthorizedException  extends  RuntimeException{
    public UnAuthorizedException(String message) {
        super(message);
    }
}
