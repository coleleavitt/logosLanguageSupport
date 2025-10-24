package com.coleleavitt.logos

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.File

class TestRealFile {
    @Test
    fun `test actual compliance file`() {
        val file = File("/home/cole/IdeaProjects/logosLanguageSupport/test-compliance.x")
        val input = file.readText()

        println("File length: ${input.length}")

        val lexer = LogosLexer(input)
        val tokens = lexer.tokenize()

        var lastEnd = 0
        tokens.forEachIndexed { index, token ->
            if (token.offset != lastEnd) {
                println("❌ GAP at token $index! Last token ended at $lastEnd, this token starts at ${token.offset}")
                val missing = input.substring(lastEnd.coerceAtMost(input.length), token.offset.coerceAtMost(input.length))
                println("   Missing text: '${missing.replace("\n", "\\n")}'")
                println("   Missing chars: ${missing.toCharArray().joinToString(" ") { "'$it' (${it.code})" }}")
            }
            if (index < 20 || token.offset != lastEnd) {
                println("Token $index: ${token.type.name.padEnd(25)} '${token.text.take(20).replace("\n", "\\n")}' offset=${token.offset} end=${token.endOffset}")
            }
            lastEnd = token.endOffset
        }

        println("\nFinal check:")
        println("Last token ended at: $lastEnd")
        println("Input length: ${input.length}")

        if (lastEnd != input.length) {
            println("❌ ERROR! Lexer stopped at offset $lastEnd, but input length is ${input.length}")
            val remaining = input.substring(lastEnd.coerceAtMost(input.length))
            println("   Remaining text: '${remaining.take(100).replace("\n", "\\n")}'")
        } else {
            println("✓ Complete coverage")
        }

        assertEquals(input.length, lastEnd, "Lexer must consume all characters")
    }
}
