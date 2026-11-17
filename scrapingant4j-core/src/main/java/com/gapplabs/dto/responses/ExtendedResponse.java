package com.gapplabs.dto.responses;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record ExtendedResponse(
        String html,
        String text,
        String cookies,
        @SerializedName("status_code") int statusCode,
        List<Header> headers,
        List<XhrResponse> xhrs,
        List<IframeResponse> iframes
) {

    public record Header(String name, String value) {}

    public record XhrResponse(
            String url,
            Integer status,
            String method,
            List<Header> headers,
            @SerializedName("request_body") String requestBody,
            String body
    ) {}

    public record IframeResponse(String src, String html) {}
}
