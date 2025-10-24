# Lexer Debug Analysis

## Current Status

Added enhanced debug output to trace the exact token flow through the lexer adapter.

## Debug Output Format

When you open a `.x` file, you'll now see detailed output like:

```
╔═══ LEXER START ═══
║ startOffset=0, endOffset=90, length=90
║ buffer.length=90
║ Generated 30 tokens
║ Last token: offset=85-90 (absolute: 85-90)
╚═══════════════════
║ First token: WHITESPACE at 0-5
║ ADVANCE: currentToken=WHITESPACE at 0-5
║ After advance: DIRECTIVE_HOOK at 5-10
║ ADVANCE: currentToken=DIRECTIVE_HOOK at 5-10
║ After advance: IDENTIFIER at 10-18
... (continues for all tokens)
║ ADVANCE: currentToken=DIRECTIVE_END at 85-90
╔═══ LEXER END ═══
║ Final position: 90-90
║ Expected end: 90
║ Match: true
╚═════════════════
```

## What to Look For

### ✅ Correct Behavior

If everything works, you should see:
1. **All tokens are contiguous** - No gaps between token end and next token start
2. **Last token ends at endOffset** - The final token's `endOffset` matches the buffer's `endOffset`
3. **Final position match** - When `currentToken` becomes null, both `getTokenStart()` and `getTokenEnd()` return `endOffset`
4. **Match: true** - The final check shows the lexer consumed everything

### ❌ Problem Indicators

If you see the error, look for:

1. **Gap in tokens:**
   ```
   ║ ADVANCE: currentToken=FOO at 10-15
   ║ After advance: BAR at 20-25  ← GAP: 15-20 not covered!
   ```

2. **Last token doesn't reach endOffset:**
   ```
   ║ Last token: offset=80-85 (absolute: 80-85)
   ║ Expected end: 90  ← Missing 5 characters!
   ```

3. **Final position mismatch:**
   ```
   ╔═══ LEXER END ═══
   ║ Final position: 0-90  ← Should be 90-90!
   ║ Expected end: 90
   ║ Match: false
   ```

## Root Cause Investigation

Based on the debug output showing `Last reported end: 0`, the issue is likely:

### Hypothesis 1: Token Offset Calculation
The `LogosLexer` might be computing `endOffset` incorrectly. Check `/app/src/main/kotlin/com/coleleavitt/logos/LogosLexer.kt`:

```kotlin
private fun addToken(type: LogosTokenType, text: String, line: Int, column: Int, offset: Int) {
    val actualLength = position - offset  // ← Is this correct?
    tokens.add(LogosToken(type, text, line, column, offset, actualLength))
}
```

The token's `endOffset` is calculated as `offset + length`. If `position - offset` is wrong, tokens won't align.

### Hypothesis 2: Missing Tokens
If the lexer doesn't tokenize ALL characters (e.g., skips whitespace at EOF), the last token won't reach `endOffset`.

### Hypothesis 3: LogosToken.endOffset Property
Check how `LogosToken.endOffset` is defined:

```kotlin
data class LogosToken(
    val type: LogosTokenType,
    val text: String,
    val line: Int,
    val column: Int,
    val offset: Int,
    val length: Int
) {
    val endOffset: Int get() = offset + length  // ← Must be correct
}
```

## Testing Steps

1. **Install updated plugin** with enhanced debug output
2. **Open a simple .x file** (even just one character: `%`)
3. **Check IDE log** (Help → Show Log in Finder/Explorer)
4. **Look for the debug box output** and analyze token flow
5. **Report findings** - paste the debug output for analysis

## Expected Fix

Once we identify which hypothesis is correct from the debug output, the fix will be one of:

- **Fix token length calculation** in `LogosLexer.addToken()`
- **Ensure all characters are tokenized** (including trailing whitespace/EOF)
- **Fix endOffset computation** in `LogosToken`
- **Handle edge cases** in the lexer adapter

## Quick Test File

Create `/tmp/test.x` with:
```
%
```

This single-character file should produce:
```
╔═══ LEXER START ═══
║ startOffset=0, endOffset=1, length=1
║ buffer.length=1
║ Generated 1 tokens
║ Last token: offset=0-1 (absolute: 0-1)
╚═══════════════════
║ First token: DIRECTIVE_HOOK at 0-1 (or ERROR if % alone isn't valid)
║ ADVANCE: currentToken=... at 0-1
╔═══ LEXER END ═══
║ Final position: 1-1  ← CRITICAL: Both must be 1
║ Expected end: 1
║ Match: true  ← MUST BE TRUE
╚═════════════════
```

If `Match: false`, we've found the bug!

## Current Build

- **Plugin:** logos-intellij-plugin-1.0.0.zip (11M)
- **Location:** `logos-intellij-plugin/build/distributions/`
- **Changes:** Enhanced debug output in LogosLexerAdapter
- **Install:** Settings → Plugins → Install from Disk → Restart

---

**Next:** Install plugin, open `.x` file, check logs for debug output, report findings.
