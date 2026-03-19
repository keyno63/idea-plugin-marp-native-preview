# IDEA Plugin Marp Native Preview (IntelliJ)

This is a native IntelliJ Platform plugin that renders Marp-like slides from markdown without `marp-cli`.

## What it does

- Adds a tool window: `Marp Native Preview`
- Reads the active markdown editor (`.md`, `.markdown`, `.mdown`)
- Parses front matter and requires `marp: true`
- Splits slides by `---`
- Renders markdown to HTML with an internal Java renderer (`commonmark`)
- Shows slides in JCEF with keyboard navigation (Left/Right, PgUp/PgDn, Home/End)

## Install for users

1. Open the Releases page:
  - https://github.com/keyno63/idea-plugin-marp-native-preview/releases
2. Download the ZIP file from the release marked **Latest**.
3. In IntelliJ IDEA, open:
  - `Settings` > `Plugins`
4. Click the gear icon in the Plugins screen, then choose:
  - `Install Plugin from Disk...`
5. Select the downloaded ZIP file and install it.

## Project layout

- `src/main/java/com/keyno/marpnative/MarpNativeToolWindowFactory.java`
- `src/main/java/com/keyno/marpnative/MarpPreviewPanel.java`
- `src/main/java/com/keyno/marpnative/MarpSlideRenderer.java`
- `src/main/resources/META-INF/plugin.xml`

## Build and run

Compatibility target:

- IntelliJ IDEA Community / Ultimate 253+ (built against IntelliJ IDEA 2025.3 platform)

Requirements:

- Gradle runtime JDK 21-23 (recommended: JDK 21)
  - Unsupported: JDK >= 24 (for example JDK 25 can fail with `Type T not present`)
- Gradle (or add wrapper later)

Commands:

```bash
gradle runIde
```

PowerShell example with JDK 21:

```powershell
$env:JAVA_HOME='C:\Users\<you>\.jdks\corretto-21.0.3'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat runIde
```

Package ZIP:

```bash
gradle buildPlugin
```

PowerShell example with JDK 21:

```powershell
$env:JAVA_HOME='C:\Users\<you>\.jdks\corretto-21.0.3'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat buildPlugin
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

## License

This project is licensed under the Apache License 2.0.  
See [LICENSE](LICENSE) for details.
