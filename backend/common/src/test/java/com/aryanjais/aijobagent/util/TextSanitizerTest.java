package com.aryanjais.aijobagent.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TextSanitizerTest {

    @Test
    void sanitize_removesHtmlTags() {
        assertEquals("Hello World", TextSanitizer.sanitize("<p>Hello <b>World</b></p>"));
    }

    @Test
    void sanitize_decodesEntities() {
        assertEquals("A & B", TextSanitizer.sanitize("A &amp; B"));
        assertEquals("\"quote\"", TextSanitizer.sanitize("&quot;quote&quot;"));
    }

    @Test
    void sanitize_collapsesWhitespace() {
        assertEquals("Hello World", TextSanitizer.sanitize("Hello    World"));
        assertEquals("Hello World", TextSanitizer.sanitize("Hello\n\t  World"));
    }

    @Test
    void sanitize_nullOrBlank_returnsEmpty() {
        assertEquals("", TextSanitizer.sanitize(null));
        assertEquals("", TextSanitizer.sanitize(""));
        assertEquals("", TextSanitizer.sanitize("   "));
    }

    @Test
    void truncate_withinLimit_returnsOriginal() {
        assertEquals("Hello", TextSanitizer.truncate("Hello", 10));
    }

    @Test
    void truncate_exceedsLimit_truncates() {
        assertEquals("Hel", TextSanitizer.truncate("Hello", 3));
    }

    @Test
    void truncate_null_returnsNull() {
        assertNull(TextSanitizer.truncate(null, 10));
    }
}
