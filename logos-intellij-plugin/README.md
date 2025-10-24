# Logos Language Support - IntelliJ Plugin

Native IntelliJ IDEA plugin for Logos language support (.x, .xm, .xi, .xmi files).

## Features

### ✅ Complete Feature Set

- **Syntax Highlighting**: 5-level color scheme for Logos directives
  - Block directives (`%hook`, `%subclass`, `%group`)
  - Special directives (`%new`, `%orig`, `%init`)
  - Lifecycle hooks (`%ctor`, `%dtor`)
  - Advanced features (`%property`, `%hookf`, `%config`)
  - Runtime functions (`%c()`)

- **Code Completion**: Smart autocomplete for all 14 Logos directives and common Objective-C classes

- **Hover Documentation**: Detailed inline documentation with examples for every directive

- **Editor Features**:
  - Comment toggling (Cmd/Ctrl + /)
  - Brace matching for {}, (), <>
  - Code folding for directive blocks
  - Structure view / document outline
  - Find usages

- **Templates**:
  - 12 live templates (code snippets) for common patterns
  - 3 file templates for quick project setup

- **Theos Integration**:
  - Build action (Ctrl+Alt+B)
  - Deploy action (Ctrl+Alt+D)
  - Run configuration support

## Building

```bash
# From project root
./gradlew :logos-intellij-plugin:buildPlugin

# Output
logos-intellij-plugin/build/distributions/logos-intellij-plugin-1.0.0.zip
```

## Installation

1. Build the plugin using the command above
2. Open IntelliJ IDEA
3. Go to **Settings → Plugins**
4. Click the gear icon (⚙️) → **Install Plugin from Disk...**
5. Select `logos-intellij-plugin-1.0.0.zip`
6. Restart IntelliJ IDEA
7. Open any `.x`, `.xm`, `.xi`, or `.xmi` file

## Usage

### Quick Start

1. **New → Logos Tweak File** to create from template
2. Type `%ho` and press Tab to insert a `%hook` block
3. Hover over any Logos directive to see documentation
4. Use **Ctrl+Alt+B** to build with Theos
5. Use **Ctrl+Alt+D** to deploy to device

### Live Templates

All live templates trigger with Tab completion:

- `hook` - Create a hook block
- `subclass` - Create a subclass
- `group` - Create a group
- `new` - Add new method
- `ctor` - Constructor block
- `property` - Add property
- `orig` - Call original implementation
- `log` - Log method call
- And more...

### Color Customization

Customize syntax colors in **Settings → Editor → Color Scheme → Logos**

## Architecture

This plugin uses a **lexer-based approach** for syntax highlighting and code completion:

- **LogosLexerAdapter**: Wraps the shared LogosLexer from the LSP server
- **Minimal Parser**: Uses `LogosParserDefinition` with lexer-only mode
- **Shared Code**: Reuses token definitions and lexer logic from the `app` module

This approach provides excellent performance while maintaining consistency with the LSP server.

## Future Enhancements

- Integration with lsp4ij for LSP features
- Full AST-based parser for advanced refactorings
- Cross-file symbol resolution
- Theos project wizard
- Device deployment GUI

## License

GPL-3.0 (matching Theos/Logos licensing)

## See Also

- [Main Project README](../README.md)
- [IntelliJ Setup Guide](../INTELLIJ_SETUP.md)
- [Project Summary](../PROJECT_SUMMARY.md)
