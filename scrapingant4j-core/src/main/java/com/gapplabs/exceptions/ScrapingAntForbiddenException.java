package com.gapplabs.exceptions;

public class ScrapingAntForbiddenException extends ScrapingAntException {
    public ScrapingAntForbiddenException(int statusCode, String message) {
        super(statusCode, message);
    }
}

