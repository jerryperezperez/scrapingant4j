package com.gapplabs.exceptions;

public class ScrapingAntLockedException extends ScrapingAntException {
    public ScrapingAntLockedException(int statusCode, String message) {
        super(statusCode, message);
    }
}
