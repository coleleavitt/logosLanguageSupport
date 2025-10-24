package com.coleleavitt.logos

fun main() {
    val testCases = listOf(
        "%hook MyClass",
        "- (void)method",
        "+ (void)method",
        "int x = 5 + 3",
        "// comment",
        "/* multi\nline */",
        "@interface MyClass"
    )

    testCases.forEach { input ->
        println("\n=== Testing: '$input' (length ${input.length}) ===")
        val lexer = LogosLexer(input)
        val tokens = lexer.tokenize()

        var lastEnd = 0
        tokens.forEach { token ->
            if (token.offset != lastEnd) {
                println("GAP! Last token ended at $lastEnd, this token starts at ${token.offset}")
            }
            println("Token: ${token.type} '${token.text}' offset=${token.offset} end=${token.endOffset}")
            lastEnd = token.endOffset
        }

        if (lastEnd != input.length) {
            println("ERROR! Lexer stopped at offset $lastEnd, but input length is ${input.length}")
            println("Missing characters: '${input.substring(lastEnd)}'")
        } else {
            println("✓ Complete coverage")
        }
    }
}
