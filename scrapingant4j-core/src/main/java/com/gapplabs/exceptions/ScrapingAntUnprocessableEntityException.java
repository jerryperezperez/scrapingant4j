package com.gapplabs.exceptions;

public class ScrapingAntUnprocessableEntityException extends ScrapingAntException {
    public ScrapingAntUnprocessableEntityException(int statusCode, String message) {
        super(statusCode, message);
    }
}
