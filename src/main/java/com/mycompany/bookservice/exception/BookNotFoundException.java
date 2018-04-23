package com.mycompany.bookservice.exception;

public class BookNotFoundException extends Exception {

    public BookNotFoundException(String message) {
        super(message);
    }

}
