package com.gapplabs.dto.responses;

import com.google.gson.annotations.SerializedName;

public record MarkdownResponse(
        @SerializedName("url") String url,
        @SerializedName("markdown") String markdown
) {}

