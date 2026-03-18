package com.enterprise.fraud.exception;

/**
 * Custom exception for when the ML model is not loaded or unavailable.
 */
public class ModelNotLoadedException extends RuntimeException {

    public ModelNotLoadedException(String message) {
        super(message);
    }
}
