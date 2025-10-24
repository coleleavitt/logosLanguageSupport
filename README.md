# Logos Language Support for IntelliJ IDEA

Complete language support for Logos (Theos preprocessor) in JetBrains IDEs.

## Features

- **Syntax Highlighting** - Comprehensive highlighting for all Logos directives
- **Code Completion** - Smart completion for directives and Objective-C classes
- **Live Templates** - Quick snippets for common Logos patterns
- **Language Server** - Full LSP integration for diagnostics, hover, and more
- **File Templates** - Create new Logos files from templates
- **Code Folding** - Fold %hook, %subclass, and %group blocks
- **Theos Integration** - Build and deploy actions

## Supported File Types

- `.x` - Logos files
- `.xm` - Logos with Objective-C++
- `.xi` - Logos interface files
- `.xmi` - Logos interface with Objective-C++

## Project Structure

```
logos LanguageSupport/
├── app/                          # LSP Server (bundled with plugin)
│   └── src/main/kotlin/
│       └── com/coleleavitt/logos/
│           ├── LogosLanguageServer.kt
│           ├── LogosLexer.kt      # Custom lexer for LSP
│           └── ...
│
└── logos-intellij-plugin/        # IntelliJ IDEA Plugin
    ├── src/main/
    │   ├── grammer/
    │   │   └── Logos.flex         # JFlex lexer specification
    │   ├── kotlin/
    │   │   └── com/coleleavitt/logos/intellij/
    │   │       ├── LogosLanguage.kt
    │   │       ├── LogosFileType.kt
    │   │       ├── LogosSyntaxHighlighter.kt
    │   │       ├── LogosCompletionContributor.kt
    │   │       └── lsp/
    │   │           └── LogosLspServerSupportProvider.kt
    │   └── resources/
    │       ├── META-INF/
    │       │   └── plugin.xml
    │       ├── icons/
    │       └── liveTemplates/
    └── build/
        └── distributions/
            └── logos-intellij-plugin-1.0.0.zip
```

## Building

### Prerequisites

- JDK 21+
- Gradle 8.14+

### Build Plugin

```bash
./gradlew :logos-intellij-plugin:buildPlugin
```

The plugin will be built to `logos-intellij-plugin/build/distributions/logos-intellij-plugin-1.0.0.zip`.

### Install Plugin

1. Open your JetBrains IDE (RustRover 2024.3+ recommended)
2. Go to **Settings → Plugins**
3. Click **⚙️ → Install Plugin from Disk**
4. Select the built `.zip` file
5. Restart the IDE

## Development

### Running Plugin in Development

```bash
./gradlew :logos-intellij-plugin:runIde
```

This will start a new IDE instance with the plugin installed.

### Debugging

1. Run the `runIde` task
2. Attach your debugger to the IDE process
3. Set breakpoints in the plugin code

### Generating Lexer

The JFlex lexer is automatically generated during build. To manually regenerate:

```bash
./gradlew :logos-intellij-plugin:generateLexer
```

### Testing

```bash
# Run all tests
./gradlew test

# Run plugin tests only
./gradlew :logos-intellij-plugin:test

# Run LSP server tests
./gradlew :app:test
```

## Architecture

### IntelliJ Plugin

The plugin provides IDE integration:
- Custom language definition (`LogosLanguage`)
- JFlex-based lexer for syntax highlighting
- Completion contributor for code completion
- LSP server provider for advanced features
- File type associations and templates

### LSP Server

The language server (in `app/`) provides:
- Custom Kotlin lexer for tokenization
- AST parsing
- Symbol table for navigation
- Diagnostics for errors/warnings
- Code completion suggestions
- Hover documentation

### Why Two Lexers?

- **JFlex Lexer** (Plugin): Fast, efficient syntax highlighting in the IDE
- **Custom Lexer** (LSP): More flexible, provides detailed tokens for language features

## Logos Syntax

### Directives

- `%hook` - Hook into existing Objective-C class
- `%subclass` - Create a subclass
- `%group` - Group multiple hooks
- `%new` - Add new method to class
- `%orig` - Call original method implementation
- `%log` - Log method call
- `%init` - Initialize groups
- `%ctor` - Constructor block (runs on load)
- `%dtor` - Destructor block (runs on unload)
- `%property` - Add property to class
- `%config` - Configuration options
- `%hookf` - Hook C functions
- `%c` - Get class reference
- `%end` - End block

### Example

```logos
%hook SpringBoard

- (void)applicationDidFinishLaunching:(id)application {
    %log;
    %orig;
    NSLog(@"SpringBoard finished launching!");
}

%new
- (void)customMethod {
    NSLog(@"This is a new method!");
}

%end

%ctor {
    %init;
    NSLog(@"Tweak loaded!");
}
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests: `./gradlew test`
5. Submit a pull request

## License

[Add your license here]

## Links

- [Theos Documentation](https://theos.dev)
- [Logos Syntax Reference](https://theos.dev/docs/logos-syntax)
- [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
