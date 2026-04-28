package com.gapplabs.exceptions;

public class ScrapingAntAuthException extends ScrapingAntException {
    public ScrapingAntAuthException(int statusCode, String message) {
        super(statusCode, message);
    }
}

