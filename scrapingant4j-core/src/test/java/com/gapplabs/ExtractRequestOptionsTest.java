package com.gapplabs;

import com.gapplabs.dto.ExtractRequestOptions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExtractRequestOptionsTest {

    @Test
    void testBuilderNormalizesExtractProperties() {
        ExtractRequestOptions options = ExtractRequestOptions.builder()
                .extractProperties("  product title ,  price(number)  , full   description ")
                .build();

        assertEquals("product title, price(number), full description", options.getExtractProperties());
    }

    @Test
    void testFromListNormalizesIntoCanonicalString() {
        ExtractRequestOptions options = ExtractRequestOptions.fromList(
                List.of(" product title ", "price(number)", "full  description ")
        );

        assertEquals("product title, price(number), full description", options.getExtractProperties());
    }

    @Test
    void testToQueryMapUsesExtractPropertiesKey() {
        ExtractRequestOptions options = ExtractRequestOptions.fromString("title, price");

        Map<String, Object> map = options.toQueryMap();

        assertEquals("title, price", map.get("extract_properties"));
    }

    @Test
    void testBuilderRejectsBlankValue() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                ExtractRequestOptions.builder().extractProperties("   ").build()
        );
        assertEquals("'extractProperties' is required and cannot be blank", ex.getMessage());
    }

    @Test
    void testBuilderRejectsEmptyToken() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                ExtractRequestOptions.fromString("title, ,price")
        );
        assertEquals("'extractProperties' contains an empty token", ex.getMessage());
    }

    @Test
    void testFromListRejectsEmptyList() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                ExtractRequestOptions.fromList(List.of())
        );
        assertEquals("'extractProperties' list is required and cannot be null or empty", ex.getMessage());
    }
}
