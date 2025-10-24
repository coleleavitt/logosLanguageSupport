package com.coleleavitt.logos

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class LogosLexerCoverageTest {

    @Test
    fun `test single character is fully covered`() {
        val input = "%"
        val lexer = LogosLexer(input)
        val tokens = lexer.tokenize()

        println("Input: '$input' (length=${input.length})")
        tokens.forEach { token ->
            println("  Token: ${token.type} at ${token.offset}-${token.endOffset} (length=${token.length})")
        }

        // Check that tokens cover the entire input
        assertTrue(tokens.isNotEmpty(), "Should generate at least one token")

        val lastToken = tokens.last()
        assertEquals(input.length, lastToken.endOffset,
            "Last token should end at input length (${input.length}), but ends at ${lastToken.endOffset}")
    }

    @Test
    fun `test tokens are contiguous`() {
        val input = "%hook Test\n%end"
        val lexer = LogosLexer(input)
        val tokens = lexer.tokenize()

        println("Input: '${input.replace("\n", "\\n")}' (length=${input.length})")
        tokens.forEach { token ->
            println("  Token: ${token.type} '${token.text.replace("\n", "\\n")}' at ${token.offset}-${token.endOffset}")
        }

        // Check all tokens are contiguous
        for (i in 0 until tokens.size - 1) {
            val current = tokens[i]
            val next = tokens[i + 1]
            assertEquals(current.endOffset, next.offset,
                "Gap between token $i (${current.type}) and ${i+1} (${next.type}): " +
                "${current.endOffset} != ${next.offset}")
        }

        // Check first token starts at 0
        assertEquals(0, tokens.first().offset, "First token should start at offset 0")

        // Check last token ends at input length
        assertEquals(input.length, tokens.last().endOffset,
            "Last token should end at input length")
    }

    @Test
    fun `test empty input`() {
        val input = ""
        val lexer = LogosLexer(input)
        val tokens = lexer.tokenize()

        println("Input: '' (length=0)")
        println("Tokens: ${tokens.size}")

        // Empty input should produce no tokens (or just EOF)
        // The adapter should handle this gracefully
        assertTrue(tokens.isEmpty() || (tokens.size == 1 && tokens[0].type == LogosTokenType.EOF),
            "Empty input should produce no tokens or just EOF")
    }

    @Test
    fun `test whitespace only`() {
        val input = "   "
        val lexer = LogosLexer(input)
        val tokens = lexer.tokenize()

        println("Input: '   ' (length=${input.length})")
        tokens.forEach { token ->
            println("  Token: ${token.type} at ${token.offset}-${token.endOffset}")
        }

        assertTrue(tokens.isNotEmpty(), "Whitespace should be tokenized")
        assertEquals(input.length, tokens.last().endOffset,
            "Whitespace tokens should cover entire input")
    }

    @Test
    fun `test newline at end`() {
        val input = "%hook\n"
        val lexer = LogosLexer(input)
        val tokens = lexer.tokenize()

        println("Input: '%hook\\n' (length=${input.length})")
        tokens.forEach { token ->
            println("  Token: ${token.type} '${token.text.replace("\n", "\\n")}' at ${token.offset}-${token.endOffset}")
        }

        assertEquals(input.length, tokens.last().endOffset,
            "Should tokenize newline at end of input")
    }
}
