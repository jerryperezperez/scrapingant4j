package com.gapplabs.exceptions;

public class ScrapingAntNotFoundException extends ScrapingAntException {
    public ScrapingAntNotFoundException(int statusCode, String message) {
        super(statusCode, message);
    }
}
