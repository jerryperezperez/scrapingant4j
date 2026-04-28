package com.gapplabs.exceptions;

public class ScrapingAntMethodNotAllowedException extends ScrapingAntException {
    public ScrapingAntMethodNotAllowedException(int statusCode, String message) {
        super(statusCode, message);
    }
}
