package com.coleleavitt.logos.intellij

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import com.intellij.psi.TokenType
import com.coleleavitt.logos.LogosTokenType

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
 * Adapter to use our Logos lexer with IntelliJ's API
 * Following JetBrains pattern from hirschgarten/ProjectViewLexer.kt
 */
class LogosLexerAdapter : com.intellij.lexer.LexerBase() {
    private var endOffset = 0
    private var offsetStart = 0
    private var buffer = ""
    private var tokens: Iterator<com.coleleavitt.logos.LogosToken> = emptyList<com.coleleavitt.logos.LogosToken>().iterator()
    private var currentToken: com.coleleavitt.logos.LogosToken? = null
    private var tokenCount = 0

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer.toString()
        this.offsetStart = startOffset
        this.endOffset = endOffset
        this.tokenCount = 0

        val source = buffer.subSequence(startOffset, endOffset).toString()
        val tokenList = com.coleleavitt.logos.LogosLexer(source).tokenize()

        System.err.println("╔═══ LEXER START ═══")
        System.err.println("║ startOffset=$startOffset, endOffset=$endOffset, length=${endOffset - startOffset}")
        System.err.println("║ buffer.length=${buffer.length}")
        System.err.println("║ Generated ${tokenList.size} tokens")
        if (tokenList.isNotEmpty()) {
            val lastToken = tokenList.last()
            System.err.println("║ Last token: offset=${lastToken.offset}-${lastToken.endOffset} (absolute: ${lastToken.offset + startOffset}-${lastToken.endOffset + startOffset})")
        }
        System.err.println("╚═══════════════════")

        tokens = tokenList.iterator()
        currentToken = if (tokens.hasNext()) tokens.next() else null

        // Debug: print initial token state
        if (currentToken != null) {
            System.err.println("║ First token: ${currentToken!!.type} at ${getTokenStart()}-${getTokenEnd()}")
        }
    }

    /** LogosLexer doesn't use states, so we return 0 as described in the Lexer docs. */
    override fun getState(): Int = 0

    override fun getTokenType(): IElementType? = currentToken?.let { LogosElementTypes.fromLogosTokenType(it.type) }

    override fun getTokenStart(): Int = currentToken?.offset?.plus(offsetStart) ?: endOffset

    override fun getTokenEnd(): Int {
        return currentToken?.endOffset?.plus(offsetStart) ?: endOffset
    }

    override fun advance() {
        System.err.println("║ ADVANCE: currentToken=${currentToken?.type} at ${getTokenStart()}-${getTokenEnd()}")

        val oldToken = currentToken
        currentToken = if (currentToken != null && tokens.hasNext()) {
            tokens.next()
        } else {
            null
        }

        if (currentToken != null) {
            System.err.println("║ After advance: ${currentToken!!.type} at ${getTokenStart()}-${getTokenEnd()}")
        } else if (oldToken != null) {
            System.err.println("╔═══ LEXER END ═══")
            System.err.println("║ Final position: ${getTokenStart()}-${getTokenEnd()}")
            System.err.println("║ Expected end: $endOffset")
            System.err.println("║ Match: ${getTokenEnd() == endOffset}")
            System.err.println("╚═════════════════")
        }

        tokenCount++
    }

    override fun getBufferSequence(): CharSequence = buffer

    override fun getBufferEnd(): Int = endOffset
}

/**
 * Element types for Logos tokens
 */
object LogosElementTypes {
    val DIRECTIVE_HOOK = IElementType("DIRECTIVE_HOOK", LogosLanguage.INSTANCE)
    val DIRECTIVE_SUBCLASS = IElementType("DIRECTIVE_SUBCLASS", LogosLanguage.INSTANCE)
    val DIRECTIVE_GROUP = IElementType("DIRECTIVE_GROUP", LogosLanguage.INSTANCE)
    val DIRECTIVE_NEW = IElementType("DIRECTIVE_NEW", LogosLanguage.INSTANCE)
    val DIRECTIVE_ORIG = IElementType("DIRECTIVE_ORIG", LogosLanguage.INSTANCE)
    val DIRECTIVE_LOG = IElementType("DIRECTIVE_LOG", LogosLanguage.INSTANCE)
    val DIRECTIVE_CTOR = IElementType("DIRECTIVE_CTOR", LogosLanguage.INSTANCE)
    val DIRECTIVE_DTOR = IElementType("DIRECTIVE_DTOR", LogosLanguage.INSTANCE)
    val DIRECTIVE_INIT = IElementType("DIRECTIVE_INIT", LogosLanguage.INSTANCE)
    val DIRECTIVE_END = IElementType("DIRECTIVE_END", LogosLanguage.INSTANCE)
    val DIRECTIVE_CONFIG = IElementType("DIRECTIVE_CONFIG", LogosLanguage.INSTANCE)
    val DIRECTIVE_PROPERTY = IElementType("DIRECTIVE_PROPERTY", LogosLanguage.INSTANCE)
    val DIRECTIVE_HOOKF = IElementType("DIRECTIVE_HOOKF", LogosLanguage.INSTANCE)
    val DIRECTIVE_C = IElementType("DIRECTIVE_C", LogosLanguage.INSTANCE)

    val OBJ_C_METHOD_SCOPE = IElementType("OBJ_C_METHOD_SCOPE", LogosLanguage.INSTANCE)
    val IDENTIFIER = IElementType("IDENTIFIER", LogosLanguage.INSTANCE)
    val STRING = IElementType("STRING", LogosLanguage.INSTANCE)
    val NUMBER = IElementType("NUMBER", LogosLanguage.INSTANCE)
    val COMMENT = IElementType("COMMENT", LogosLanguage.INSTANCE)

    val LBRACE = IElementType("LBRACE", LogosLanguage.INSTANCE)
    val RBRACE = IElementType("RBRACE", LogosLanguage.INSTANCE)
    val LPAREN = IElementType("LPAREN", LogosLanguage.INSTANCE)
    val RPAREN = IElementType("RPAREN", LogosLanguage.INSTANCE)
    val LANGLE = IElementType("LANGLE", LogosLanguage.INSTANCE)
    val RANGLE = IElementType("RANGLE", LogosLanguage.INSTANCE)
    val LBRACKET = IElementType("LBRACKET", LogosLanguage.INSTANCE)
    val RBRACKET = IElementType("RBRACKET", LogosLanguage.INSTANCE)
    val SEMICOLON = IElementType("SEMICOLON", LogosLanguage.INSTANCE)
    val COLON = IElementType("COLON", LogosLanguage.INSTANCE)
    val COMMA = IElementType("COMMA", LogosLanguage.INSTANCE)
    val SLASH = IElementType("SLASH", LogosLanguage.INSTANCE)

    fun fromLogosTokenType(tokenType: LogosTokenType): IElementType? {
        return when (tokenType) {
            LogosTokenType.WHITESPACE -> TokenType.WHITE_SPACE
            LogosTokenType.ERROR -> TokenType.BAD_CHARACTER
            LogosTokenType.DIRECTIVE_HOOK -> DIRECTIVE_HOOK
            LogosTokenType.DIRECTIVE_SUBCLASS -> DIRECTIVE_SUBCLASS
            LogosTokenType.DIRECTIVE_GROUP -> DIRECTIVE_GROUP
            LogosTokenType.DIRECTIVE_NEW -> DIRECTIVE_NEW
            LogosTokenType.DIRECTIVE_ORIG -> DIRECTIVE_ORIG
            LogosTokenType.DIRECTIVE_LOG -> DIRECTIVE_LOG
            LogosTokenType.DIRECTIVE_CTOR -> DIRECTIVE_CTOR
            LogosTokenType.DIRECTIVE_DTOR -> DIRECTIVE_DTOR
            LogosTokenType.DIRECTIVE_INIT -> DIRECTIVE_INIT
            LogosTokenType.DIRECTIVE_END -> DIRECTIVE_END
            LogosTokenType.DIRECTIVE_CONFIG -> DIRECTIVE_CONFIG
            LogosTokenType.DIRECTIVE_PROPERTY -> DIRECTIVE_PROPERTY
            LogosTokenType.DIRECTIVE_HOOKF -> DIRECTIVE_HOOKF
            LogosTokenType.DIRECTIVE_C -> DIRECTIVE_C
            LogosTokenType.OBJ_C_METHOD_SCOPE -> OBJ_C_METHOD_SCOPE
            LogosTokenType.IDENTIFIER -> IDENTIFIER
            LogosTokenType.STRING -> STRING
            LogosTokenType.NUMBER -> NUMBER
            LogosTokenType.COMMENT -> COMMENT
            LogosTokenType.LBRACE -> LBRACE
            LogosTokenType.RBRACE -> RBRACE
            LogosTokenType.LPAREN -> LPAREN
            LogosTokenType.RPAREN -> RPAREN
            LogosTokenType.LANGLE -> LANGLE
            LogosTokenType.RANGLE -> RANGLE
            LogosTokenType.LBRACKET -> LBRACKET
            LogosTokenType.RBRACKET -> RBRACKET
            LogosTokenType.SEMICOLON -> SEMICOLON
            LogosTokenType.COLON -> COLON
            LogosTokenType.COMMA -> COMMA
            LogosTokenType.SLASH -> SLASH
            else -> null
        }
    }
}
