package com.example.demo.exception;

public class JourneyNotFoundException extends RuntimeException{
    public JourneyNotFoundException(String message){
        super(message);
    }
}
