package com.example.springretrytesting;

public class CustomException extends RuntimeException {
    public CustomException(String message) {
        super(message);
    }
}
