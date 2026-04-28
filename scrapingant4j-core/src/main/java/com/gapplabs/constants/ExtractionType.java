package com.gapplabs.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExtractionType {
    
    GENERAL("general"),
    MARKDOWN("markdown"),
    JSON("extended"),
    AI_EXTRACTED("extract");
    
    private final String value;
}
