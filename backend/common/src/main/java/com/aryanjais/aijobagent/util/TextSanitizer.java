package com.aryanjais.aijobagent.util;

/**
 * Utility to strip HTML tags and normalise whitespace from text content.
 */
public final class TextSanitizer {

    private TextSanitizer() {
    }

    /**
     * Remove HTML tags, decode common entities, collapse whitespace.
     */
    public static String sanitize(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        // Strip HTML tags
        String cleaned = text.replaceAll("<[^>]+>", " ");
        // Decode common HTML entities
        cleaned = cleaned.replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&nbsp;", " ")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");
        // Collapse multiple whitespace into single space
        cleaned = cleaned.replaceAll("\\s+", " ");
        return cleaned.trim();
    }

    /**
     * Truncate text to a maximum length.
     */
    public static String truncate(String text, int maxLength) {
        if (text == null) {
            return null;
        }
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }
}
