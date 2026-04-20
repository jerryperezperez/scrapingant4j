package com.gapplabs.dto;

import com.gapplabs.constants.BlockResource;
import com.gapplabs.constants.ProxyCountry;
import com.gapplabs.constants.ProxyType;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.charset.StandardCharsets;
// TODO: Migrate these DTO classes to record classes
@Getter
@Builder
@ToString
public class ScrapingAntRequest {
    
    // Requerido
    @NotBlank
    private final String url;
    @NonNull
    private Boolean browser;
    @NonNull
    private Boolean returnPageSource;
    @NonNull
    private ProxyType proxyType;
    @NonNull
    private ProxyCountry proxyCountry;
    private String cookies;
    private String jsSnippet; // El usuario pasa JS plano, nosotros encodeamos
    private String waitForSelector;
    private String cssSelector;
    private List<BlockResource> blockResources;
    private Integer timeout; // Rango 5-60
    
    @SuppressWarnings("unused")
    public static class ScrapingAntRequestBuilder {
        @SuppressWarnings({"ConstantConditions"})
        public ScrapingAntRequest build() {
            // Explicit required-fields validation with clear messages (i18n-friendly: English)
            if (this.proxyType == null) {
                throw new IllegalArgumentException("'proxyType' is required and cannot be null");
            }
            if (this.proxyCountry == null) {
                throw new IllegalArgumentException("'proxyCountry' is required and cannot be null");
            }
            if (this.browser == null) {
                throw new IllegalArgumentException("'browser' is required and cannot be null");
            }
            if (this.returnPageSource == null) {
                throw new IllegalArgumentException("'returnPageSource' is required and cannot be null");
            }
             if ((this.browser == null || !this.browser) && this.returnPageSource) {
                throw new IllegalArgumentException("returnPageSource can only be true when browser is true");
             }
             
             if ((this.browser == null || !this.browser) && this.jsSnippet != null) {
                throw new IllegalArgumentException("jsSnippet can only be set when browser is true");
             }
             
            if ((this.browser == null || !this.browser) && this.cssSelector != null) {
                throw new IllegalArgumentException("cssSelector can only be set when browser is true");
             }
            return new ScrapingAntRequest(url, browser, returnPageSource, proxyType, proxyCountry, cookies, jsSnippet, waitForSelector, cssSelector, blockResources, timeout);
         }
     }
     
     
     /**
      * Construye el mapa de parámetros para OpenFeign.
      * Los valores nulos no se agregan, permitiendo que la API use sus defaults.
      */
     public Map<String, Object> toQueryMap() {
         Map<String, Object> params = new HashMap<>();
         
         params.put("url", this.url);
         
         if (!browser) {
             params.put("browser", browser);
         }
         if (timeout != null) {
             params.put("timeout", timeout);
         }
         if (returnPageSource){
            params.put("return_page_source", returnPageSource);
         }
         if (cookies != null) {
             params.put("cookies", cookies);
         }
         if (proxyType != ProxyType.STANDARD) {
             params.put("proxy_type", proxyType.getValue());
         }
         if (proxyCountry != ProxyCountry.ALL_COUNTRIES) {
             params.put("proxy_country", proxyCountry.getValue());
         }
         if (waitForSelector != null) {
             params.put("wait_for_selector", waitForSelector);
         }
         
         // Codificación automática a Base64 del JS Snippet
        if (jsSnippet != null && !jsSnippet.isEmpty()) {
            params.put("js_snippet", Base64.getEncoder().encodeToString(jsSnippet.getBytes(StandardCharsets.UTF_8)));
         }
         
        // Map CSS selector if present
        if (cssSelector != null && !cssSelector.isEmpty()) {
            params.put("css_selector", cssSelector);
        }
         /* * Manejo de parámetros repetidos para block_resource.
          * Al pasar una List a Feign en un QueryMap, Feign genera
          * la estructura repetida: block_resource=v1&block_resource=v2
          */
         if (blockResources != null && !blockResources.isEmpty()) {
             List<String> resources = blockResources.stream().map(BlockResource::getValue).toList();
             params.put("block_resource", resources);
         }
         
         return params;
     }
     
 }
