package com.keyno.marpnative;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MarpSlideRenderer {
    private static final Pattern FRONT_MATTER_SPLIT = Pattern.compile("(?s)^---\\R(.*?)\\R---\\R?");
    private static final Pattern SLIDE_SEPARATOR = Pattern.compile("(?m)^---\\s*$");
    private static final Pattern DIRECTIVE = Pattern.compile("^([A-Za-z0-9_-]+)\\s*:\\s*(.*)$");

    private final Parser parser = Parser.builder().build();
    private final HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();

    public String render(String markdown) {
        ParsedMarp parsed = parseMarp(markdown);
        if (!isMarpEnabled(parsed.frontMatter)) {
            return renderMessage(
                    "Marp front matter is not enabled.",
                    "Add `marp: true` in the top front matter block to render as slides."
            );
        }

        List<String> slidesHtml = new ArrayList<>();
        for (String slideMarkdown : parsed.slides) {
            Node node = parser.parse(slideMarkdown);
            slidesHtml.add(htmlRenderer.render(node));
        }
        if (slidesHtml.isEmpty()) {
            slidesHtml.add("<h1>Empty slide deck</h1><p>Add markdown content below front matter.</p>");
        }

        return buildHtml(slidesHtml, parsed.frontMatter);
    }

    private static ParsedMarp parseMarp(String markdown) {
        Map<String, String> frontMatter = new LinkedHashMap<>();
        String body = markdown == null ? "" : markdown;

        Matcher frontMatterMatcher = FRONT_MATTER_SPLIT.matcher(body);
        if (frontMatterMatcher.find()) {
            String block = frontMatterMatcher.group(1);
            body = body.substring(frontMatterMatcher.end());
            for (String line : block.split("\\R")) {
                Matcher directiveMatcher = DIRECTIVE.matcher(line.trim());
                if (directiveMatcher.matches()) {
                    frontMatter.put(directiveMatcher.group(1).toLowerCase(), directiveMatcher.group(2).trim());
                }
            }
        }

        String[] rawSlides = SLIDE_SEPARATOR.split(body, -1);
        List<String> slides = new ArrayList<>(rawSlides.length);
        for (String slide : rawSlides) {
            slides.add(slide.trim());
        }
        return new ParsedMarp(frontMatter, slides);
    }

    private static boolean isMarpEnabled(Map<String, String> frontMatter) {
        String marp = frontMatter.getOrDefault("marp", "false").toLowerCase();
        return "true".equals(marp) || "yes".equals(marp) || "1".equals(marp);
    }

    private static String buildHtml(List<String> slidesHtml, Map<String, String> frontMatter) {
        String theme = frontMatter.getOrDefault("theme", "default");
        String title = escapeHtml(frontMatter.getOrDefault("title", "Marp Native Preview"));
        String paginate = frontMatter.getOrDefault("paginate", "false");
        boolean showPage = "true".equalsIgnoreCase(paginate) || "yes".equalsIgnoreCase(paginate);

        StringBuilder sections = new StringBuilder();
        for (int i = 0; i < slidesHtml.size(); i++) {
            sections.append("<section class=\"slide\" data-index=\"").append(i).append("\">");
            sections.append(slidesHtml.get(i));
            if (showPage) {
                sections.append("<footer class=\"page\">").append(i + 1).append(" / ").append(slidesHtml.size()).append("</footer>");
            }
            sections.append("</section>");
        }

        String css = "default".equalsIgnoreCase(theme) ? defaultThemeCss() : defaultThemeCss();
        return "<!doctype html><html><head><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<title>" + title + "</title><style>" + css + "</style></head><body>"
                + "<div id=\"deck\">" + sections + "</div>"
                + "<script>" + navigationJs() + "</script></body></html>";
    }

    private static String renderMessage(String title, String message) {
        return "<!doctype html><html><head><meta charset=\"utf-8\"><style>"
                + "body{font-family:Segoe UI,Arial,sans-serif;background:#f7f7f8;color:#222;padding:24px;}"
                + "h2{margin:0 0 8px;}p{margin:0;color:#444;}</style></head><body>"
                + "<h2>" + escapeHtml(title) + "</h2><p>" + escapeHtml(message) + "</p></body></html>";
    }

    private static String defaultThemeCss() {
        return """
                :root {
                    --bg: #ffffff;
                    --fg: #0f172a;
                    --accent: #0ea5e9;
                }
                * { box-sizing: border-box; }
                html, body {
                    margin: 0;
                    width: 100%;
                    height: 100%;
                    overflow: hidden;
                    background: #0b1020;
                    color: var(--fg);
                    font-family: "Segoe UI", "Hiragino Sans", sans-serif;
                }
                #deck {
                    width: 100%;
                    height: 100%;
                    position: relative;
                }
                .slide {
                    display: none;
                    width: 100%;
                    height: 100%;
                    padding: 64px;
                    background:
                        radial-gradient(circle at top right, rgba(14, 165, 233, 0.10), transparent 45%),
                        linear-gradient(180deg, #f9fafb, #eef2ff);
                    overflow: auto;
                }
                .slide.active {
                    display: block;
                }
                h1, h2, h3 {
                    margin-top: 0;
                    color: #020617;
                }
                p, li, blockquote, code {
                    font-size: 1.05rem;
                    line-height: 1.55;
                }
                pre {
                    background: #111827;
                    color: #e5e7eb;
                    padding: 16px;
                    border-radius: 10px;
                    overflow: auto;
                }
                code {
                    font-family: "JetBrains Mono", monospace;
                }
                blockquote {
                    border-left: 4px solid var(--accent);
                    padding-left: 12px;
                    margin-left: 0;
                    color: #334155;
                }
                img {
                    max-width: 100%;
                    max-height: 62vh;
                    border-radius: 10px;
                }
                .page {
                    position: absolute;
                    right: 24px;
                    bottom: 16px;
                    color: #64748b;
                    font-size: 0.9rem;
                }
                """;
    }

    private static String navigationJs() {
        return """
                (() => {
                    const slides = Array.from(document.querySelectorAll('.slide'));
                    let idx = 0;
                    const show = (n) => {
                        idx = Math.max(0, Math.min(n, slides.length - 1));
                        slides.forEach((s, i) => s.classList.toggle('active', i === idx));
                    };
                    show(0);
                    window.addEventListener('keydown', (e) => {
                        if (['ArrowRight', 'PageDown', ' '].includes(e.key)) {
                            show(idx + 1);
                            e.preventDefault();
                        } else if (['ArrowLeft', 'PageUp'].includes(e.key)) {
                            show(idx - 1);
                            e.preventDefault();
                        } else if (e.key === 'Home') {
                            show(0);
                        } else if (e.key === 'End') {
                            show(slides.length - 1);
                        }
                    });
                })();
                """;
    }

    private static String escapeHtml(String input) {
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private record ParsedMarp(Map<String, String> frontMatter, List<String> slides) {
    }
}
