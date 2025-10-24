package com.coleleavitt.logos

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class Test90Chars {
    @Test
    fun `test exactly 90 characters`() {
        val input = "// Logos Compliance Test File\n// Tests all directives and advanced syntax patterns\n\n#impor"

        println("Input length: ${input.length}")
        println("Input: '$input'")

        val lexer = LogosLexer(input)
        val tokens = lexer.tokenize()

        var lastEnd = 0
        tokens.forEachIndexed { index, token ->
            if (token.offset != lastEnd) {
                println("❌ GAP at token $index! Last ended at $lastEnd, this starts at ${token.offset}")
                val gap = input.substring(lastEnd, token.offset)
                println("   Gap content: '${gap.replace("\n", "\\n")}'")
                println("   Gap bytes: ${gap.toCharArray().joinToString(" ") { "'$it'(${it.code})" }}")
            }
            println("Token $index: ${token.type.name.padEnd(25)} offset=${token.offset}-${token.endOffset} '${token.text.replace("\n", "\\n")}'")
            lastEnd = token.endOffset
        }

        println("\nFinal: lastEnd=$lastEnd, input.length=${input.length}")

        if (lastEnd != input.length) {
            val remaining = input.substring(lastEnd)
            println("❌ Missing: '${remaining}'")
            println("   Missing bytes: ${remaining.toCharArray().joinToString(" ") { "'$it'(${it.code})" }}")
        }

        assertEquals(input.length, lastEnd, "Must consume all characters")
    }
}
