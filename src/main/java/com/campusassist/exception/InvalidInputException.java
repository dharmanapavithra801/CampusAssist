package com.campusassist.exception;

/** Thrown when the user types something we cannot accept (empty title, bad id, ...). */
public class InvalidInputException extends RuntimeException {
    public InvalidInputException(String message) {
        super(message);
    }
}
