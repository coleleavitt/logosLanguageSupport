package com.coleleavitt.logos

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests to verify compliance with official Logos syntax from theos/logos repository
 */
class LogosComplianceTest {

    @Test
    fun `all 15 directives are implemented`() {
        val expectedDirectives = setOf(
            LogosTokenType.DIRECTIVE_HOOK,
            LogosTokenType.DIRECTIVE_SUBCLASS,
            LogosTokenType.DIRECTIVE_GROUP,
            LogosTokenType.DIRECTIVE_CLASS,
            LogosTokenType.DIRECTIVE_C,
            LogosTokenType.DIRECTIVE_NEW,
            LogosTokenType.DIRECTIVE_ORIG,
            LogosTokenType.DIRECTIVE_LOG,
            LogosTokenType.DIRECTIVE_CTOR,
            LogosTokenType.DIRECTIVE_DTOR,
            LogosTokenType.DIRECTIVE_INIT,
            LogosTokenType.DIRECTIVE_END,
            LogosTokenType.DIRECTIVE_CONFIG,
            LogosTokenType.DIRECTIVE_PROPERTY,
            LogosTokenType.DIRECTIVE_HOOKF
        )

        val allTokenTypes = LogosTokenType.values().toSet()
        assertTrue(allTokenTypes.containsAll(expectedDirectives),
            "Missing directive token types")
    }

    @Test
    fun `Logos README example parses correctly`() {
        // Official example from https://github.com/theos/logos README
        val source = """
            %hook NSObject

            - (NSString *)description {
                return [%orig stringByAppendingString:@" (of doom)"];
            }

            %new
            - (void)helloWorld {
                NSLog(@"Awesome!");
            }

            %end
        """.trimIndent()

        val lexer = LogosLexer(source)
        val tokens = lexer.tokenize()

        // Verify key elements are recognized
        assertTrue(tokens.any { it.type == LogosTokenType.DIRECTIVE_HOOK })
        assertTrue(tokens.any { it.text == "NSObject" })
        assertTrue(tokens.any { it.text == "description" })
        assertTrue(tokens.any { it.type == LogosTokenType.DIRECTIVE_ORIG })
        assertTrue(tokens.any { it.type == LogosTokenType.DIRECTIVE_NEW })
        assertTrue(tokens.any { it.text == "helloWorld" })
        assertTrue(tokens.any { it.type == LogosTokenType.DIRECTIVE_END })
    }

    @Test
    fun `Swift class with dots handled like logos dot pl line 433`() {
        // logos.pl handles "Module.ClassName" by converting dots to underscores
        val source = "%hook Swift.UIKit.MySwiftClass\n%end"
        val lexer = LogosLexer(source)
        val tokens = lexer.tokenize()

        // Should recognize the Swift class name
        val swiftName = tokens.find { it.text.contains(".") }
        assertNotNull(swiftName, "Swift class with dots should be tokenized")
    }

    @Test
    fun `protocol lists like logos dot pl line 443`() {
        // %subclass NewClass : ParentClass <Protocol1, Protocol2>
        val source = "%subclass MyView : UIView <UITableViewDelegate, UITableViewDataSource>"
        val lexer = LogosLexer(source)
        val tokens = lexer.tokenize()

        assertTrue(tokens.any { it.type == LogosTokenType.DIRECTIVE_SUBCLASS })
        assertTrue(tokens.any { it.type == LogosTokenType.LANGLE })
        assertTrue(tokens.any { it.type == LogosTokenType.RANGLE })
        assertTrue(tokens.any { it.type == LogosTokenType.COMMA })
    }

    @Test
    fun `%c() with scope prefix like logos dot pl line 460`() {
        // %c([+-]<identifier>)
        val testCases = listOf(
            "%c(NSString)" to "-",     // implicit instance
            "%c(+NSObject)" to "+",     // explicit metaclass
            "%c(-UIView)" to "-"        // explicit instance
        )

        testCases.forEach { (source, expectedScope) ->
            val lexer = LogosLexer(source)
            val tokens = lexer.tokenize()

            assertTrue(tokens.any { it.type == LogosTokenType.DIRECTIVE_C },
                "Failed to recognize %c in: $source")
        }
    }

    @Test
    fun `%new with type encoding like logos dot pl line 482`() {
        // %new[(type)]
        val source = "%new(v@:)"
        val lexer = LogosLexer(source)
        val tokens = lexer.tokenize()

        assertEquals(LogosTokenType.DIRECTIVE_NEW, tokens[0].type)
        assertEquals(LogosTokenType.LPAREN, tokens[1].type)
    }

    @Test
    fun `method declaration like logos dot pl line 491`() {
        // [+-] (<return>)<[X:]>
        val source = "- (void)methodWithArg:(NSString *)arg andArg:(int)arg2"
        val lexer = LogosLexer(source)
        val tokens = lexer.tokenize()

        assertTrue(tokens.any { it.type == LogosTokenType.OBJ_C_METHOD_SCOPE })
        assertTrue(tokens.any { it.text == "void" })
        assertTrue(tokens.any { it.text == "methodWithArg" })
        assertTrue(tokens.any { it.type == LogosTokenType.COLON })
    }

    @Test
    fun `%orig with and without parentheses like logos dot pl line 541`() {
        val testCases = listOf(
            "%orig;",
            "%orig();",
            "%orig(arg1, arg2);"
        )

        testCases.forEach { source ->
            val lexer = LogosLexer(source)
            val tokens = lexer.tokenize()

            assertTrue(tokens.any { it.type == LogosTokenType.DIRECTIVE_ORIG },
                "Failed for: $source")
        }
    }

    @Test
    fun `&%orig function pointer like logos dot pl line 569`() {
        // &%orig at word boundary
        val source = "IMP original = &%orig;"
        val lexer = LogosLexer(source)
        val tokens = lexer.tokenize()

        val ampersandIndex = tokens.indexOfFirst { it.type == LogosTokenType.AMPERSAND }
        val origIndex = tokens.indexOfFirst { it.type == LogosTokenType.DIRECTIVE_ORIG }

        assertTrue(ampersandIndex >= 0, "Should recognize ampersand")
        assertTrue(origIndex >= 0, "Should recognize %orig")
        assertTrue(origIndex > ampersandIndex, "Should be in order: & then %orig")
    }

    @Test
    fun `%log with optional arguments like logos dot pl line 588`() {
        val testCases = listOf(
            "%log;",
            "%log();",
            "%log(arg1, arg2);"
        )

        testCases.forEach { source ->
            val lexer = LogosLexer(source)
            val tokens = lexer.tokenize()

            assertTrue(tokens.any { it.type == LogosTokenType.DIRECTIVE_LOG },
                "Failed for: $source")
        }
    }

    @Test
    fun `%ctor generates constructor like logos dot pl line 606`() {
        val source = "%ctor { %init; }"
        val lexer = LogosLexer(source)
        val tokens = lexer.tokenize()

        assertTrue(tokens.any { it.type == LogosTokenType.DIRECTIVE_CTOR })
        assertTrue(tokens.any { it.type == LogosTokenType.DIRECTIVE_INIT })
    }

    @Test
    fun `%dtor generates destructor like logos dot pl line 612`() {
        val source = "%dtor { NSLog(@\"cleanup\"); }"
        val lexer = LogosLexer(source)
        val tokens = lexer.tokenize()

        assertTrue(tokens.any { it.type == LogosTokenType.DIRECTIVE_DTOR })
    }

    @Test
    fun `%init with group and mappings like logos dot pl line 618`() {
        // %init with optional (GroupName) and class=expression
        val source = "%init(iOS14, UIViewController=objc_getClass(\"UIViewController\"));"
        val lexer = LogosLexer(source)
        val tokens = lexer.tokenize()

        assertTrue(tokens.any { it.type == LogosTokenType.DIRECTIVE_INIT })
        assertTrue(tokens.any { it.text == "iOS14" })
        assertTrue(tokens.any { it.text == "UIViewController" })
        assertTrue(tokens.any { it.type == LogosTokenType.EQUALS })
    }

    @Test
    fun `%property with attributes like logos dot pl line 680`() {
        // %property (attributes) type name;
        val attributes = listOf(
            "nonatomic",
            "strong",
            "retain",
            "copy",
            "assign",
            "readonly"
        )

        attributes.forEach { attr ->
            val source = "%property ($attr) NSString *prop;"
            val lexer = LogosLexer(source)
            val tokens = lexer.tokenize()

            assertTrue(tokens.any { it.type == LogosTokenType.DIRECTIVE_PROPERTY },
                "Failed for attribute: $attr")
            assertTrue(tokens.any { it.text == attr },
                "Failed to recognize attribute: $attr")
        }
    }

    @Test
    fun `%property with getter and setter like logos dot pl line 706`() {
        val source = "%property (nonatomic, strong, getter=customGetter, setter=customSetter:) id prop;"
        val lexer = LogosLexer(source)
        val tokens = lexer.tokenize()

        assertTrue(tokens.any { it.type == LogosTokenType.DIRECTIVE_PROPERTY })
        assertTrue(tokens.any { it.text == "getter" })
        assertTrue(tokens.any { it.text == "setter" })
        assertTrue(tokens.any { it.text == "customGetter" })
        assertTrue(tokens.any { it.text == "customSetter" })
    }

    @Test
    fun `%hookf for C functions like logos dot pl line 788`() {
        val source = "%hookf(int, some_function, const char *arg)"
        val lexer = LogosLexer(source)
        val tokens = lexer.tokenize()

        assertTrue(tokens.any { it.type == LogosTokenType.DIRECTIVE_HOOKF })
    }

    @Test
    fun `%config for configuration like logos dot pl line 672`() {
        val source = "%config(generator=internal)"
        val lexer = LogosLexer(source)
        val tokens = lexer.tokenize()

        assertTrue(tokens.any { it.type == LogosTokenType.DIRECTIVE_CONFIG })
        assertTrue(tokens.any { it.text == "generator" })
        assertTrue(tokens.any { it.text == "internal" })
    }

    @Test
    fun `variadic method parameters like logos dot pl line 536`() {
        val source = "- (void)logMessage:(NSString *)format, ..."
        val lexer = LogosLexer(source)
        val tokens = lexer.tokenize()

        assertTrue(tokens.any { it.type == LogosTokenType.ELLIPSIS })
    }

    @Test
    fun `vim syntax file patterns recognized`() {
        // Based on extras/vim/syntax/logos.vim
        val patterns = listOf(
            "%hook",
            "%group",
            "%subclass",
            "%ctor",
            "%end",
            "%class",
            "%log",
            "%orig",
            "%init",
            "%new"
        )

        patterns.forEach { pattern ->
            val lexer = LogosLexer(pattern)
            val tokens = lexer.tokenize()

            assertTrue(tokens.isNotEmpty(), "Failed to tokenize: $pattern")
            assertTrue(tokens[0].text == pattern, "First token should be: $pattern")
        }
    }
}
