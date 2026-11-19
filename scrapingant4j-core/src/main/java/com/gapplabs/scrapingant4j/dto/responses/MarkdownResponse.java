package com.gapplabs.scrapingant4j.dto.responses;

import com.google.gson.annotations.SerializedName;

public record MarkdownResponse(
    @SerializedName("url") String url, @SerializedName("markdown") String markdown) {
  @Override
  public String toString() {
    return "MarkdownResponse[markdownLength=" + (markdown == null ? 0 : markdown.length()) + "]";
  }
}
