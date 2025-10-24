package com.coleleavitt.logos.intellij

import java.io.File

fun main() {
    println("=== Testing Lexer Adapter ===\n")

    // Test 1: Simple text
    println("Test 1: Simple text")
    testLexer("// comment\n%hook Test\n%end")

    println("\n" + "=".repeat(50) + "\n")

    // Test 2: The actual file
    println("Test 2: test-compliance.x file")
    val file = File("/home/cole/IdeaProjects/logosLanguageSupport/test-compliance.x")
    if (file.exists()) {
        testLexer(file.readText())
    } else {
        println("File not found!")
    }
}

fun testLexer(text: String) {
    println("Text length: ${text.length}")
    println("First 100 chars: ${text.take(100).replace("\n", "\\n")}")
    println()

    val lexer = LogosLexerAdapter()
    lexer.start(text, 0, text.length, 0)

    var lastTokenEnd = 0
    var tokenCount = 0
    var hasError = false

    while (lexer.tokenType != null) {
        val start = lexer.tokenStart
        val end = lexer.tokenEnd
        val type = lexer.tokenType

        if (tokenCount < 10 || tokenCount % 100 == 0) {
            val tokenText = text.substring(start, end).replace("\n", "\\n")
            println("Token $tokenCount: type=$type, start=$start, end=$end, text='$tokenText'")
        }

        // Check continuity
        if (start != lastTokenEnd) {
            println("❌ ERROR: Gap detected! Last token ended at $lastTokenEnd, this starts at $start")
            val gap = text.substring(lastTokenEnd, start)
            println("   Gap content: '${gap.replace("\n", "\\n")}'")
            hasError = true
        }

        lastTokenEnd = end
        tokenCount++
        lexer.advance()
    }

    println("\nTotal tokens: $tokenCount")
    println("Last token ended at: $lastTokenEnd")
    println("Text length: ${text.length}")

    // This is the exact check IntelliJ does
    if (text.length > 0 && lastTokenEnd != text.length) {
        println("❌ FAIL: Unexpected termination offset for lexer. Last token ended at $lastTokenEnd but text length is ${text.length}")
        val remaining = text.substring(lastTokenEnd)
        println("   Remaining text: '${remaining.replace("\n", "\\n")}'")
        println("   Remaining length: ${remaining.length}")
        hasError = true
    } else {
        println("✅ PASS: Last token ends exactly at text length")
    }

    if (!hasError) {
        println("✅ All checks passed!")
    }
}
