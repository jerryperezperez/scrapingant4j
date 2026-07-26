package com.gapplabs.exceptions;

public class ScrapingAntConflictException extends ScrapingAntException {
    public ScrapingAntConflictException(int statusCode, String message) {
        super(statusCode, message);
    }
}
