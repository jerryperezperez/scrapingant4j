package com.gapplabs.exceptions;

public class ScrapingAntServerException extends ScrapingAntException {
    public ScrapingAntServerException(int statusCode, String message) {
        super(statusCode, message);
    }
}

