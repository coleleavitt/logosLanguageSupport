package com.coleleavitt.logos.intellij

import com.intellij.lexer.Lexer
import com.intellij.lexer.FlexAdapter
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import com.intellij.psi.TokenType

/**
 * Syntax highlighter for Logos language
 */
class LogosSyntaxHighlighter : SyntaxHighlighterBase() {

    override fun getHighlightingLexer(): Lexer {
        return LogosLexerAdapter()
    }

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        return when {
            tokenType == LogosElementTypes.DIRECTIVE_HOOK ||
            tokenType == LogosElementTypes.DIRECTIVE_SUBCLASS ||
            tokenType == LogosElementTypes.DIRECTIVE_GROUP ||
            tokenType == LogosElementTypes.DIRECTIVE_END -> {
                arrayOf(LOGOS_DIRECTIVE)
            }

            tokenType == LogosElementTypes.DIRECTIVE_NEW ||
            tokenType == LogosElementTypes.DIRECTIVE_ORIG ||
            tokenType == LogosElementTypes.DIRECTIVE_LOG ||
            tokenType == LogosElementTypes.DIRECTIVE_INIT -> {
                arrayOf(LOGOS_SPECIAL)
            }

            tokenType == LogosElementTypes.DIRECTIVE_CTOR ||
            tokenType == LogosElementTypes.DIRECTIVE_DTOR -> {
                arrayOf(LOGOS_LIFECYCLE)
            }

            tokenType == LogosElementTypes.DIRECTIVE_PROPERTY ||
            tokenType == LogosElementTypes.DIRECTIVE_HOOKF ||
            tokenType == LogosElementTypes.DIRECTIVE_CONFIG -> {
                arrayOf(LOGOS_ADVANCED)
            }

            tokenType == LogosElementTypes.DIRECTIVE_C -> {
                arrayOf(LOGOS_RUNTIME)
            }

            tokenType == LogosElementTypes.OBJ_C_METHOD_SCOPE -> {
                arrayOf(OBJC_METHOD_SCOPE)
            }

            tokenType == LogosElementTypes.IDENTIFIER -> {
                arrayOf(DefaultLanguageHighlighterColors.IDENTIFIER)
            }

            tokenType == LogosElementTypes.STRING -> {
                arrayOf(DefaultLanguageHighlighterColors.STRING)
            }

            tokenType == LogosElementTypes.NUMBER -> {
                arrayOf(DefaultLanguageHighlighterColors.NUMBER)
            }

            tokenType == LogosElementTypes.COMMENT -> {
                arrayOf(DefaultLanguageHighlighterColors.LINE_COMMENT)
            }

            tokenType == LogosElementTypes.LBRACE ||
            tokenType == LogosElementTypes.RBRACE -> {
                arrayOf(DefaultLanguageHighlighterColors.BRACES)
            }

            tokenType == LogosElementTypes.LPAREN ||
            tokenType == LogosElementTypes.RPAREN -> {
                arrayOf(DefaultLanguageHighlighterColors.PARENTHESES)
            }

            tokenType == LogosElementTypes.LANGLE ||
            tokenType == LogosElementTypes.RANGLE ||
            tokenType == LogosElementTypes.LBRACKET ||
            tokenType == LogosElementTypes.RBRACKET -> {
                arrayOf(DefaultLanguageHighlighterColors.BRACKETS)
            }

            tokenType == LogosElementTypes.SEMICOLON ||
            tokenType == LogosElementTypes.COLON ||
            tokenType == LogosElementTypes.COMMA -> {
                arrayOf(DefaultLanguageHighlighterColors.COMMA)
            }

            else -> emptyArray()
        }
    }

    companion object {
        val LOGOS_DIRECTIVE = TextAttributesKey.createTextAttributesKey(
            "LOGOS_DIRECTIVE",
            DefaultLanguageHighlighterColors.KEYWORD
        )

        val LOGOS_SPECIAL = TextAttributesKey.createTextAttributesKey(
            "LOGOS_SPECIAL",
            DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL
        )

        val LOGOS_LIFECYCLE = TextAttributesKey.createTextAttributesKey(
            "LOGOS_LIFECYCLE",
            DefaultLanguageHighlighterColors.METADATA
        )

        val LOGOS_ADVANCED = TextAttributesKey.createTextAttributesKey(
            "LOGOS_ADVANCED",
            DefaultLanguageHighlighterColors.STATIC_METHOD
        )

        val LOGOS_RUNTIME = TextAttributesKey.createTextAttributesKey(
            "LOGOS_RUNTIME",
            DefaultLanguageHighlighterColors.INSTANCE_METHOD
        )

        val OBJC_METHOD_SCOPE = TextAttributesKey.createTextAttributesKey(
            "OBJC_METHOD_SCOPE",
            DefaultLanguageHighlighterColors.OPERATION_SIGN
        )
    }
}

/**
 * Adapter to use JFlex-generated lexer with IntelliJ's API
 */
class LogosLexerAdapter : FlexAdapter(LogosFlexLexer(null))

/**
 * Element types for Logos tokens
 */
object LogosElementTypes {
    @JvmField val DIRECTIVE_HOOK = IElementType("DIRECTIVE_HOOK", LogosLanguage.INSTANCE)
    @JvmField val DIRECTIVE_SUBCLASS = IElementType("DIRECTIVE_SUBCLASS", LogosLanguage.INSTANCE)
    @JvmField val DIRECTIVE_GROUP = IElementType("DIRECTIVE_GROUP", LogosLanguage.INSTANCE)
    @JvmField val DIRECTIVE_NEW = IElementType("DIRECTIVE_NEW", LogosLanguage.INSTANCE)
    @JvmField val DIRECTIVE_ORIG = IElementType("DIRECTIVE_ORIG", LogosLanguage.INSTANCE)
    @JvmField val DIRECTIVE_LOG = IElementType("DIRECTIVE_LOG", LogosLanguage.INSTANCE)
    @JvmField val DIRECTIVE_CTOR = IElementType("DIRECTIVE_CTOR", LogosLanguage.INSTANCE)
    @JvmField val DIRECTIVE_DTOR = IElementType("DIRECTIVE_DTOR", LogosLanguage.INSTANCE)
    @JvmField val DIRECTIVE_INIT = IElementType("DIRECTIVE_INIT", LogosLanguage.INSTANCE)
    @JvmField val DIRECTIVE_END = IElementType("DIRECTIVE_END", LogosLanguage.INSTANCE)
    @JvmField val DIRECTIVE_CONFIG = IElementType("DIRECTIVE_CONFIG", LogosLanguage.INSTANCE)
    @JvmField val DIRECTIVE_PROPERTY = IElementType("DIRECTIVE_PROPERTY", LogosLanguage.INSTANCE)
    @JvmField val DIRECTIVE_HOOKF = IElementType("DIRECTIVE_HOOKF", LogosLanguage.INSTANCE)
    @JvmField val DIRECTIVE_C = IElementType("DIRECTIVE_C", LogosLanguage.INSTANCE)

    @JvmField val OBJ_C_METHOD_SCOPE = IElementType("OBJ_C_METHOD_SCOPE", LogosLanguage.INSTANCE)
    @JvmField val IDENTIFIER = IElementType("IDENTIFIER", LogosLanguage.INSTANCE)
    @JvmField val STRING = IElementType("STRING", LogosLanguage.INSTANCE)
    @JvmField val NUMBER = IElementType("NUMBER", LogosLanguage.INSTANCE)
    @JvmField val COMMENT = IElementType("COMMENT", LogosLanguage.INSTANCE)

    @JvmField val LBRACE = IElementType("LBRACE", LogosLanguage.INSTANCE)
    @JvmField val RBRACE = IElementType("RBRACE", LogosLanguage.INSTANCE)
    @JvmField val LPAREN = IElementType("LPAREN", LogosLanguage.INSTANCE)
    @JvmField val RPAREN = IElementType("RPAREN", LogosLanguage.INSTANCE)
    @JvmField val LANGLE = IElementType("LANGLE", LogosLanguage.INSTANCE)
    @JvmField val RANGLE = IElementType("RANGLE", LogosLanguage.INSTANCE)
    @JvmField val LBRACKET = IElementType("LBRACKET", LogosLanguage.INSTANCE)
    @JvmField val RBRACKET = IElementType("RBRACKET", LogosLanguage.INSTANCE)
    @JvmField val SEMICOLON = IElementType("SEMICOLON", LogosLanguage.INSTANCE)
    @JvmField val COLON = IElementType("COLON", LogosLanguage.INSTANCE)
    @JvmField val COMMA = IElementType("COMMA", LogosLanguage.INSTANCE)
    @JvmField val SLASH = IElementType("SLASH", LogosLanguage.INSTANCE)
}
