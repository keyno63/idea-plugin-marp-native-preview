# IDEA Plugin Marp Native Preview (IntelliJ)

This is a native IntelliJ Platform plugin that renders Marp-like slides from markdown without `marp-cli`.

## What it does

- Adds a tool window: `Marp Native Preview`
- Reads the active markdown editor (`.md`, `.markdown`, `.mdown`)
- Parses front matter and requires `marp: true`
- Splits slides by `---`
- Renders markdown to HTML with an internal Java renderer (`commonmark`)
- Shows slides in JCEF with keyboard navigation (Left/Right, PgUp/PgDn, Home/End)

## Project layout

- `src/main/java/com/keyno/marpnative/MarpNativeToolWindowFactory.java`
- `src/main/java/com/keyno/marpnative/MarpPreviewPanel.java`
- `src/main/java/com/keyno/marpnative/MarpSlideRenderer.java`
- `src/main/resources/META-INF/plugin.xml`

## Build and run

Requirements:

- JDK 21
  - Unsupported: JDK >= 24 because the build chain can use it. 
- Gradle (or add wrapper later)

Commands:

```bash
gradle runIde
```

Package ZIP:

```bash
gradle buildPlugin
```

## Supported directives

Front matter keys currently used:

- `marp: true` (required)
- `title: ...`
- `theme: ...` (currently falls back to default internal theme)
- `paginate: true`

## Notes

This implementation is native and does not execute `marp-cli`.  
Compatibility is intentionally partial compared to official Marp.
