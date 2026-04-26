package com.gapplabs.exceptions;

import lombok.Getter;

/**
 * Base exception for ScrapingAnt SDK.
 * Immutable: final fields, no setters.
 */
@Getter
public class ScrapingAntException extends RuntimeException {
    private final int statusCode;
    /**
     * -- GETTER --
     *  The API provided message (parsed from the response body `detail` field when available).
     */
    private final String detailMessage;

    public ScrapingAntException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
        this.detailMessage = message;
    }
    
}
