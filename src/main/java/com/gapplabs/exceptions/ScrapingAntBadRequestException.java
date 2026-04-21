package com.gapplabs.exceptions;

public class ScrapingAntBadRequestException extends ScrapingAntException {
    public ScrapingAntBadRequestException(int statusCode, String message) {
        super(statusCode, message);
    }
}
