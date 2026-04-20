package com.gapplabs;

import com.gapplabs.constants.ExtractionType;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;
import java.util.Map;

/**
 * Interfaz interna de Feign para la comunicación con ScrapingAnt.
 */
interface ScrapingAntApi {
    
    /**
     * @param queryParams Mapa generado por ScrapingAntRequest.toQueryMap()
     * @return El contenido scrapeado y metadatos.
     */
    @RequestLine("GET /{extraction_type}")
    String scrape(
            @QueryMap Map<String, Object> queryParams,
            @Param("extraction_type") String extractionTypeValue
    );
}