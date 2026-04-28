package com.gapplabs;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class ScrapingAntAuthInterceptor implements RequestInterceptor {
    public final String API_KEY_HEADER = "x-api-key";
    private final String apiKey;
    
    public ScrapingAntAuthInterceptor(String apiKey) {
        this.apiKey = apiKey;
    }
    
    @Override
    public void apply(RequestTemplate template) {
        // Inyecta la llave en el header según la documentación
        template.header(API_KEY_HEADER, apiKey);
    }
}