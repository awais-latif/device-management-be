package com.example.devicemanagement.exception;

public class UnknownDeviceStateException extends RuntimeException {

    public UnknownDeviceStateException(String message) {
        super(message);
    }
}