package com.gapplabs.exceptions;

public class ScrapingAntRateLimitException extends ScrapingAntException {
    private final Integer retryAfter;

    public ScrapingAntRateLimitException(int statusCode, String message, Integer retryAfter) {
        super(statusCode, message);
        this.retryAfter = retryAfter;
    }

    public Integer getRetryAfter() {
        return retryAfter;
    }
}

