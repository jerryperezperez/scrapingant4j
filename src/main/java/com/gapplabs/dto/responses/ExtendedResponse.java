package com.gapplabs.dto.responses;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ExtendedResponse(
        String html,
        String text,
        String cookies,
        @SerializedName("status_code") int statusCode,  // only this one needs it
       // TODO: Convert `headers` field in `ExtendedResponse` from `List<Map<String, String>>` to `Map<String, String>` during deserialization
        List<Map<String, String>> headers,
        List<HashMap<String, String>> xhrs,
        List<HashMap<String, String>> iframes
) {}