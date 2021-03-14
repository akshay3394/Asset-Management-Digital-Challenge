package com.db.awmd.challenge.exception;

public class AccountDoesNotExistsException extends RuntimeException{

    public AccountDoesNotExistsException(String message){
        super(message);
    }
}
