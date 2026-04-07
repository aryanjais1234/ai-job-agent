package com.aryanjais.aijobagent.exception;

/**
 * Thrown when an AI service call (OpenAI) fails after all retries.
 */
public class AiServiceException extends RuntimeException {

    public AiServiceException(String message) {
        super(message);
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
