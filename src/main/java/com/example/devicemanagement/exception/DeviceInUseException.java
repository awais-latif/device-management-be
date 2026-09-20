package com.example.devicemanagement.exception;

public class DeviceInUseException extends RuntimeException {

    public DeviceInUseException(String message) {
        super(message);
    }
}
