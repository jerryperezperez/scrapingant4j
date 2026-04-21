package com.gapplabs.exceptions;

/**
 * Base exception for ScrapingAnt SDK.
 * Immutable: final fields, no setters.
 */
public class ScrapingAntException extends RuntimeException {
    private final int statusCode;
    private final String detailMessage;

    public ScrapingAntException(int statusCode, String message) {
        super(String.format("HTTP %d: %s", statusCode, message));
        this.statusCode = statusCode;
        this.detailMessage = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    /**
     * The API provided message (parsed from the response body `detail` field when available).
     */
    public String getDetailMessage() {
        return detailMessage;
    }
}

