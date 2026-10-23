package com.example.demo.exception;
public class BookingNotfoundException extends RuntimeException{
    public BookingNotfoundException(String message){
        super(message);
    }
}
