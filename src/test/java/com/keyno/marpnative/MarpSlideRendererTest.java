package com.keyno.marpnative;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarpSlideRendererTest {

    private final MarpSlideRenderer renderer = new MarpSlideRenderer();

    @Test
    void returnsHintWhenMarpFrontMatterIsMissing() {
        String markdown = "# Title\n\nNo front matter.";

        String html = renderer.render(markdown);

        assertTrue(html.contains("Marp front matter is not enabled."));
        assertTrue(html.contains("Add `marp: true`"));
    }

    @Test
    void rendersSlidesWhenMarpIsEnabled() {
        String markdown = """
                ---
                marp: true
                title: Demo Deck
                ---
                # Slide 1
                
                ---
                
                # Slide 2
                """;

        String html = renderer.render(markdown);

        assertTrue(html.contains("<title>Demo Deck</title>"));
        assertTrue(html.contains("data-index=\"0\""));
        assertTrue(html.contains("data-index=\"1\""));
        assertTrue(html.contains("<h1>Slide 1</h1>"));
        assertTrue(html.contains("<h1>Slide 2</h1>"));
    }

    @Test
    void rendersPaginationFooterWhenPaginateIsTrue() {
        String markdown = """
                ---
                marp: true
                paginate: true
                ---
                A
                ---
                B
                """;

        String html = renderer.render(markdown);

        assertTrue(html.contains("<footer class=\"page\">1 / 2</footer>"));
        assertTrue(html.contains("<footer class=\"page\">2 / 2</footer>"));
    }

    @Test
    void doesNotRenderPaginationFooterWhenPaginateIsFalse() {
        String markdown = """
                ---
                marp: true
                paginate: false
                ---
                A
                ---
                B
                """;

        String html = renderer.render(markdown);

        assertFalse(html.contains("class=\"page\""));
    }

    @Test
    void escapesHtmlInTitle() {
        String markdown = """
                ---
                marp: true
                title: <script>alert("x")</script>
                ---
                # Slide
                """;

        String html = renderer.render(markdown);

        assertTrue(html.contains("<title>&lt;script&gt;alert(&quot;x&quot;)&lt;/script&gt;</title>"));
        assertFalse(html.contains("<title><script>"));
    }
}
