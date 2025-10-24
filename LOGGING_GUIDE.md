# IntelliJ Plugin Logging Best Practices

## Current Status
✅ **No debug prints** - All `System.err.println()` statements have been removed

## How to Add Logging (IntelliJ Way)

### 1. Use IntelliJ's Logger API

```kotlin
import com.intellij.openapi.diagnostic.Logger

class LogosSyntaxHighlighter : SyntaxHighlighterBase() {
    companion object {
        private val LOG = Logger.getInstance(LogosSyntaxHighlighter::class.java)
    }

    override fun getHighlightingLexer(): Lexer {
        LOG.debug("Creating highlighting lexer")
        return LogosLexerAdapter()
    }

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        LOG.trace("Getting highlights for token: $tokenType")
        return when {
            tokenType == LogosElementTypes.DIRECTIVE_HOOK -> {
                LOG.debug("Highlighting directive: %hook")
                arrayOf(LOGOS_DIRECTIVE)
            }
            else -> emptyArray()
        }
    }
}
```

### 2. Log Levels (from verbose to critical)

```kotlin
LOG.trace("Very detailed info")     // Only in trace mode
LOG.debug("Debug information")      // Development debugging
LOG.info("Informational message")   // General info
LOG.warn("Warning message")         // Something unexpected
LOG.error("Error message")          // Error occurred
LOG.error("Error with exception", exception)  // With stack trace
```

### 3. Where Logs Appear

Logs are written to:
- **IDE Log**: `Help → Show Log in Explorer/Finder`
- **File Location**:
  - macOS: `~/Library/Logs/JetBrains/RustRover2024.3/idea.log`
  - Linux: `~/.local/share/JetBrains/RustRover2024.3/log/idea.log`
  - Windows: `%APPDATA%\JetBrains\RustRover2024.3\log\idea.log`

### 4. Enable Debug Logging

To see `LOG.debug()` messages:

1. **Option A: Via UI**
   - `Help → Diagnostic Tools → Debug Log Settings`
   - Add: `#com.coleleavitt.logos`

2. **Option B: Via File**
   Create/edit `idea.log.xml` in config directory:
   ```xml
   <idea-log>
     <category name="com.coleleavitt.logos" level="DEBUG"/>
   </idea-log>
   ```

### 5. Example: Add Logging to Completion

```kotlin
class LogosCompletionProvider : CompletionProvider<CompletionParameters>() {
    companion object {
        private val LOG = Logger.getInstance(LogosCompletionProvider::class.java)
    }

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        LOG.debug("Adding completions at offset: ${parameters.offset}")

        try {
            DIRECTIVE_COMPLETIONS.forEach { result.addElement(it) }
            OBJC_CLASS_COMPLETIONS.forEach { result.addElement(it) }

            LOG.info("Added ${DIRECTIVE_COMPLETIONS.size} directive completions")
        } catch (e: Exception) {
            LOG.error("Failed to add completions", e)
        }
    }
}
```

### 6. Performance Logging

```kotlin
class LogosAnnotator : Annotator {
    companion object {
        private val LOG = Logger.getInstance(LogosAnnotator::class.java)
    }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val startTime = System.currentTimeMillis()

        try {
            // Do annotation work
            checkSyntax(element, holder)
        } finally {
            val duration = System.currentTimeMillis() - startTime
            if (duration > 100) {
                LOG.warn("Annotation took ${duration}ms for element: ${element.text}")
            }
        }
    }
}
```

## Best Practices

### ✅ DO:
- Use `Logger.getInstance()` as a companion object
- Use appropriate log levels (debug, info, warn, error)
- Log exceptions with stack traces: `LOG.error("message", exception)`
- Use lazy evaluation for expensive strings: `LOG.debug { "Expensive: ${expensiveOperation()}" }`
- Log important state changes and errors

### ❌ DON'T:
- Use `System.out.println()` or `System.err.println()`
- Use `printStackTrace()`
- Log in hot paths without checking log level
- Log sensitive user data
- Leave debug logs in production code

## Example: Full Class with Logging

```kotlin
package com.coleleavitt.logos.intellij

import com.intellij.lexer.Lexer
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType

class LogosSyntaxHighlighter : SyntaxHighlighterBase() {

    companion object {
        private val LOG = Logger.getInstance(LogosSyntaxHighlighter::class.java)

        val LOGOS_DIRECTIVE = TextAttributesKey.createTextAttributesKey(
            "LOGOS_DIRECTIVE",
            DefaultLanguageHighlighterColors.KEYWORD
        )
    }

    override fun getHighlightingLexer(): Lexer {
        LOG.debug("Creating highlighting lexer")
        return LogosLexerAdapter()
    }

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        LOG.trace { "Highlighting token: $tokenType" }

        return when {
            tokenType == LogosElementTypes.DIRECTIVE_HOOK -> {
                LOG.debug { "Highlighting Logos directive: %hook" }
                arrayOf(LOGOS_DIRECTIVE)
            }
            else -> {
                LOG.trace { "No special highlighting for: $tokenType" }
                emptyArray()
            }
        }
    }
}
```

## Viewing Logs During Development

When running with `./gradlew :logos-intellij-plugin:runIde`:

1. Logs appear in the terminal where you ran the command
2. Also in the sandbox IDE: `Help → Show Log`
3. For detailed debugging, add this to terminal command:
   ```bash
   ./gradlew :logos-intellij-plugin:runIde --info
   ```

## Log Filtering

To filter logs in production:

```kotlin
if (LOG.isDebugEnabled) {
    LOG.debug("Expensive operation: ${expensiveStringBuilding()}")
}
```

This prevents expensive string building when debug logging is disabled.

---

## Summary

**Current plugin:** ✅ Clean, no debug prints
**Recommended:** Add proper `Logger` where needed
**Log location:** `idea.log` file in IDE config directory
**Enable debug:** Add `#com.coleleavitt.logos` to Debug Log Settings

Need logging added to any specific components? Let me know!
