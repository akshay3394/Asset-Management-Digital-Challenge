package com.db.awmd.challenge.exception;

public class InsufficientAccountBalanceException extends Exception{
    public InsufficientAccountBalanceException(String message){
        super(message);
    }
}
