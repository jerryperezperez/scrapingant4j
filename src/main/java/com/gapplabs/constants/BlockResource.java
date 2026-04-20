package com.gapplabs.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Define los tipos de recursos que pueden ser bloqueados durante el renderizado
 * para mejorar la velocidad y reducir el consumo de recursos.
 */
@Getter
@RequiredArgsConstructor
public enum BlockResource {
    IMAGE("image"),
    STYLESHEET("stylesheet"),
    FONT("font"),
    MEDIA("media"),
    SCRIPT("script"),
    TEXTTRACK("texttrack"),
    XHR("xhr"),
    FETCH("fetch"),
    EVENTSOURCE("eventsource"),
    WEBSOCKET("websocket"),
    MANIFEST("manifest"),
    OTHER("other");
    
    private final String value;
    
    @Override
    public String toString() {
        return this.value;
    }
}
