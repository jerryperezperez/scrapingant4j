package com.gapplabs;

import com.gapplabs.exceptions.ScrapingAntAuthException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScrapingAntAuthExceptionTest {

    @Test
    void constructorStoresStatusCodeAndMessage() {
        ScrapingAntAuthException exception = new ScrapingAntAuthException(401, "Unauthorized");

        assertEquals(401, exception.getStatusCode());
        assertEquals("Unauthorized", exception.getMessage());
        assertEquals("Unauthorized", exception.getDetailMessage());
    }
}
