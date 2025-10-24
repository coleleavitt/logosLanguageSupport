package com.coleleavitt.logos

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class LogosLexerTest {

    @Test
    fun `test basic hook directive`() {
        val source = "%hook NSObject\n%end"
        val lexer = LogosLexer(source)
        val tokens = lexer.tokenize()

        assertEquals(LogosTokenType.DIRECTIVE_HOOK, tokens[0].type)
        assertEquals("%hook", tokens[0].text)
        assertEquals(LogosTokenType.IDENTIFIER, tokens[2].type)
        assertEquals("NSObject", tokens[2].text)
        assertEquals(LogosTokenType.DIRECTIVE_END, tokens[4].type)
    }

    @Test
    fun `test all directives are recognized`() {
        val directives = mapOf(
            "%hook" to LogosTokenType.DIRECTIVE_HOOK,
            "%subclass" to LogosTokenType.DIRECTIVE_SUBCLASS,
            "%group" to LogosTokenType.DIRECTIVE_GROUP,
            "%class" to LogosTokenType.DIRECTIVE_CLASS,
            "%new" to LogosTokenType.DIRECTIVE_NEW,
            "%orig" to LogosTokenType.DIRECTIVE_ORIG,
            "%log" to LogosTokenType.DIRECTIVE_LOG,
            "%ctor" to LogosTokenType.DIRECTIVE_CTOR,
            "%dtor" to LogosTokenType.DIRECTIVE_DTOR,
            "%init" to LogosTokenType.DIRECTIVE_INIT,
            "%end" to LogosTokenType.DIRECTIVE_END,
            "%config" to LogosTokenType.DIRECTIVE_CONFIG,
            "%property" to LogosTokenType.DIRECTIVE_PROPERTY,
            "%hookf" to LogosTokenType.DIRECTIVE_HOOKF
        )

        directives.forEach { (text, expectedType) ->
            val lexer = LogosLexer(text)
            val tokens = lexer.tokenize()
            assertEquals(expectedType, tokens[0].type, "Failed for directive: $text")
        }
    }

    @Test
    fun `test %c() runtime class lookup`() {
        val testCases = listOf(
            "%c(NSString)",
            "%c(+NSObject)",
            "%c(-UIView)",
            "%c(Swift.Module.ClassName)"
        )

        testCases.forEach { source ->
            val lexer = LogosLexer(source)
            val tokens = lexer.tokenize()
            assertEquals(LogosTokenType.DIRECTIVE_C, tokens[0].type, "Failed for: $source")
            assertEquals(LogosTokenType.LPAREN, tokens[1].type)
        }
    }

    @Test
    fun `test method scope tokens`() {
        val source = "- (void)method + (id)classMethod"
        val lexer = LogosLexer(source)
        val tokens = lexer.tokenize()

        val scopeTokens = tokens.filter { it.type == LogosTokenType.OBJ_C_METHOD_SCOPE }
        assertEquals(2, scopeTokens.size)
        assertEquals("-", scopeTokens[0].text)
        assertEquals("+", scopeTokens[1].text)
    }

    @Test
    fun `test ampersand for function pointer`() {
        val source = "IMP ptr = &%orig;"
        val lexer = LogosLexer(source)
        val tokens = lexer.tokenize()

        val ampersand = tokens.find { it.type == LogosTokenType.AMPERSAND }
        assertNotNull(ampersand)
        assertEquals("&", ampersand?.text)

        val orig = tokens.find { it.type == LogosTokenType.DIRECTIVE_ORIG }
        assertNotNull(orig)
    }

    @Test
    fun `test Swift class with dots`() {
        val source = "%hook Swift.UIKit.MyClass\n%end"
        val lexer = LogosLexer(source)
        val tokens = lexer.tokenize()

        // Should have Swift identifier with dots
        val swiftClass = tokens.find { it.text.contains(".") }
        assertNotNull(swiftClass, "Swift class name with dots should be recognized")
    }

    @Test
    fun `test subclass with protocols`() {
        val source = "%subclass MyView : UIView <UIDelegate, UIDataSource>"
        val lexer = LogosLexer(source)
        val tokens = lexer.tokenize()

        assertEquals(LogosTokenType.DIRECTIVE_SUBCLASS, tokens[0].type)
        assertTrue(tokens.any { it.text == "MyView" })
        assertTrue(tokens.any { it.text == "UIView" })
        assertTrue(tokens.any { it.type == LogosTokenType.LANGLE })
        assertTrue(tokens.any { it.type == LogosTokenType.RANGLE })
    }

    @Test
    fun `test property attributes`() {
        val source = "%property (nonatomic, strong) NSString *title;"
        val lexer = LogosLexer(source)
        val tokens = lexer.tokenize()

        assertEquals(LogosTokenType.DIRECTIVE_PROPERTY, tokens[0].type)
        assertTrue(tokens.any { it.text == "nonatomic" })
        assertTrue(tokens.any { it.text == "strong" })
        assertTrue(tokens.any { it.text == "NSString" })
    }

    @Test
    fun `test %new with type encoding`() {
        val source = "%new(v@:)"
        val lexer = LogosLexer(source)
        val tokens = lexer.tokenize()

        assertEquals(LogosTokenType.DIRECTIVE_NEW, tokens[0].type)
        assertEquals(LogosTokenType.LPAREN, tokens[1].type)
        // Type encoding should be captured - v, @, and : are separate tokens
        // Just verify the tokens exist (lexer doesn't need to understand type encodings)
        assertTrue(tokens.any { it.text == "v" })
        assertTrue(tokens.any { it.text == ":" })
    }

    @Test
    fun `test variadic method with ellipsis`() {
        val source = "- (void)log:(NSString *)format, ..."
        val lexer = LogosLexer(source)
        val tokens = lexer.tokenize()

        val ellipsis = tokens.find { it.type == LogosTokenType.ELLIPSIS }
        assertNotNull(ellipsis, "Ellipsis (...) should be recognized")
    }

    @Test
    fun `test comments are recognized`() {
        val source = """
            // Line comment
            %hook Test
            /* Block comment */
            %end
        """.trimIndent()

        val lexer = LogosLexer(source)
        val tokens = lexer.tokenize()

        val comments = tokens.filter { it.type == LogosTokenType.COMMENT }
        assertTrue(comments.size >= 2, "Should recognize both line and block comments")
    }

    @Test
    fun `test string literals`() {
        val source = """NSLog(@"Hello World")"""
        val lexer = LogosLexer(source)
        val tokens = lexer.tokenize()

        val stringToken = tokens.find { it.type == LogosTokenType.STRING }
        assertNotNull(stringToken)
        assertTrue(stringToken?.text?.contains("Hello World") ?: false)
    }

    @Test
    fun `test braces and brackets`() {
        val source = "{ [ ( < > ) ] }"
        val lexer = LogosLexer(source)
        val tokens = lexer.tokenize()

        // Filter out whitespace to test brackets
        val nonWhitespace = tokens.filter { it.type != LogosTokenType.WHITESPACE }

        assertEquals(LogosTokenType.LBRACE, nonWhitespace[0].type)
        assertEquals(LogosTokenType.LBRACKET, nonWhitespace[1].type)
        assertEquals(LogosTokenType.LPAREN, nonWhitespace[2].type)
        assertEquals(LogosTokenType.LANGLE, nonWhitespace[3].type)
        assertEquals(LogosTokenType.RANGLE, nonWhitespace[4].type)
        assertEquals(LogosTokenType.RPAREN, nonWhitespace[5].type)
        assertEquals(LogosTokenType.RBRACKET, nonWhitespace[6].type)
        assertEquals(LogosTokenType.RBRACE, nonWhitespace[7].type)
    }

    @Test
    fun `test Objective-C keywords`() {
        val keywords = mapOf(
            "@interface" to LogosTokenType.OBJ_C_INTERFACE,
            "@implementation" to LogosTokenType.OBJ_C_IMPLEMENTATION,
            "@protocol" to LogosTokenType.OBJ_C_PROTOCOL,
            "@end" to LogosTokenType.OBJ_C_END,
            "@selector" to LogosTokenType.OBJ_C_SELECTOR,
            "@property" to LogosTokenType.OBJ_C_PROPERTY
        )

        keywords.forEach { (text, expectedType) ->
            val lexer = LogosLexer(text)
            val tokens = lexer.tokenize()
            assertEquals(expectedType, tokens[0].type, "Failed for keyword: $text")
        }
    }

    @Test
    fun `test real world hook example`() {
        val source = """
            %hook SpringBoard

            - (void)applicationDidFinishLaunching:(id)application {
                %orig;
                NSLog(@"Hooked!");
            }

            %new
            - (void)customMethod {
                Class cls = %c(MyClass);
            }

            %end
        """.trimIndent()

        val lexer = LogosLexer(source)
        val tokens = lexer.tokenize()

        // Should have all expected tokens
        assertTrue(tokens.any { it.type == LogosTokenType.DIRECTIVE_HOOK })
        assertTrue(tokens.any { it.text == "SpringBoard" })
        assertTrue(tokens.any { it.type == LogosTokenType.OBJ_C_METHOD_SCOPE })
        assertTrue(tokens.any { it.type == LogosTokenType.DIRECTIVE_ORIG })
        assertTrue(tokens.any { it.type == LogosTokenType.DIRECTIVE_NEW })
        assertTrue(tokens.any { it.type == LogosTokenType.DIRECTIVE_C })
        assertTrue(tokens.any { it.type == LogosTokenType.DIRECTIVE_END })
    }

    @Test
    fun `test %init with group name`() {
        val source = "%init(iOS14Features);"
        val lexer = LogosLexer(source)
        val tokens = lexer.tokenize()

        assertEquals(LogosTokenType.DIRECTIVE_INIT, tokens[0].type)
        assertEquals(LogosTokenType.LPAREN, tokens[1].type)
        assertTrue(tokens.any { it.text == "iOS14Features" })
    }

    @Test
    fun `test %config directive`() {
        val source = "%config(generator=internal)"
        val lexer = LogosLexer(source)
        val tokens = lexer.tokenize()

        assertEquals(LogosTokenType.DIRECTIVE_CONFIG, tokens[0].type)
        assertTrue(tokens.any { it.text == "generator" })
        assertTrue(tokens.any { it.text == "internal" })
    }

    @Test
    fun `test numbers`() {
        val source = "42 3.14 0xFF 0b101"
        val lexer = LogosLexer(source)
        val tokens = lexer.tokenize()

        val numbers = tokens.filter { it.type == LogosTokenType.NUMBER }
        assertTrue(numbers.size >= 2)
    }

    @Test
    fun `test empty source`() {
        val lexer = LogosLexer("")
        val tokens = lexer.tokenize()

        assertTrue(tokens.isEmpty() || tokens.all { it.type == LogosTokenType.EOF })
    }

    @Test
    fun `test deprecated %class directive`() {
        val source = "%class SpringBoard"
        val lexer = LogosLexer(source)
        val tokens = lexer.tokenize()

        assertEquals(LogosTokenType.DIRECTIVE_CLASS, tokens[0].type)
        // Implementation should recognize it even though it's deprecated
    }

    @Test
    fun `test complete lexer coverage`() {
        val testCases = listOf(
            "%hook MyClass",
            "- (void)method",
            "+ (void)method",
            "int x = 5 + 3",
            "// comment",
            "/* multi\nline */",
            "@interface MyClass",
            "a-b",
            "x+y",
            "{ }",
            "%hook SpringBoard\n- (void)applicationDidFinishLaunching:(id)application {\n    %orig;\n}\n%end\n"
        )

        testCases.forEach { input ->
            println("\n=== Testing: '$input' (length ${input.length}) ===")
            val lexer = LogosLexer(input)
            val tokens = lexer.tokenize()

            var lastEnd = 0
            tokens.forEach { token ->
                if (token.offset != lastEnd) {
                    println("❌ GAP! Last token ended at $lastEnd, this token starts at ${token.offset}")
                    println("   Missing: '${input.substring(lastEnd, token.offset)}'")
                }
                println("Token: ${token.type.name.padEnd(25)} '${token.text.replace("\n", "\\n")}' offset=${token.offset} end=${token.endOffset}")
                lastEnd = token.endOffset
            }

            if (lastEnd != input.length) {
                println("❌ ERROR! Lexer stopped at offset $lastEnd, but input length is ${input.length}")
                println("   Missing characters: '${input.substring(lastEnd)}'")
            } else {
                println("✓ Complete coverage")
            }

            // Assert complete coverage
            assertEquals(input.length, lastEnd, "Lexer must consume all characters in: '$input'")
        }
    }
}
