package com.coleleavitt.logos

/**
 * Lexer for Logos language
 * Tokenizes .x, .xm, .xi, and .xmi files
 */
class LogosLexer(private val source: String) {
    private var position = 0
    private var line = 0
    private var column = 0

    private val tokens = mutableListOf<LogosToken>()

    fun tokenize(): List<LogosToken> {
        tokens.clear()
        position = 0
        line = 0
        column = 0

        while (!isAtEnd()) {
            scanToken()
        }

        // Don't add EOF token - IntelliJ lexers return null when done
        return tokens
    }

    private fun scanToken() {
        val startOffset = position
        val startLine = line
        val startColumn = column

        val c = advance()

        when (c) {
            ' ', '\t', '\r', '\n' -> {
                // Tokenize whitespace - IntelliJ requires complete coverage
                val whitespaceStart = startOffset
                while (peek() in listOf(' ', '\t', '\r', '\n') && !isAtEnd()) {
                    if (advance() == '\n') {
                        line++
                        column = 0
                    }
                }
                val whitespaceText = source.substring(whitespaceStart, position)
                addToken(LogosTokenType.WHITESPACE, whitespaceText, startLine, startColumn, whitespaceStart)
            }
            '/' -> {
                if (match('/')) {
                    // Single-line comment - must tokenize, not skip
                    val commentStart = startOffset
                    while (peek() != '\n' && !isAtEnd()) advance()
                    val commentText = source.substring(commentStart, position)
                    addToken(LogosTokenType.COMMENT, commentText, startLine, startColumn, commentStart)
                } else if (match('*')) {
                    // Multi-line comment - must tokenize, not skip
                    val commentStart = startOffset
                    while (!isAtEnd()) {
                        if (peek() == '*' && peekNext() == '/') {
                            advance() // *
                            advance() // /
                            break
                        }
                        if (advance() == '\n') {
                            line++
                            column = 0
                        }
                    }
                    val commentText = source.substring(commentStart, position)
                    addToken(LogosTokenType.COMMENT, commentText, startLine, startColumn, commentStart)
                } else {
                    // Regular division operator
                    addToken(LogosTokenType.SLASH, "/", startLine, startColumn, startOffset)
                }
            }
            ':' -> addToken(LogosTokenType.COLON, ":", startLine, startColumn, startOffset)
            ';' -> addToken(LogosTokenType.SEMICOLON, ";", startLine, startColumn, startOffset)
            ',' -> addToken(LogosTokenType.COMMA, ",", startLine, startColumn, startOffset)
            '(' -> addToken(LogosTokenType.LPAREN, "(", startLine, startColumn, startOffset)
            ')' -> addToken(LogosTokenType.RPAREN, ")", startLine, startColumn, startOffset)
            '{' -> addToken(LogosTokenType.LBRACE, "{", startLine, startColumn, startOffset)
            '}' -> addToken(LogosTokenType.RBRACE, "}", startLine, startColumn, startOffset)
            '<' -> addToken(LogosTokenType.LANGLE, "<", startLine, startColumn, startOffset)
            '>' -> addToken(LogosTokenType.RANGLE, ">", startLine, startColumn, startOffset)
            '[' -> addToken(LogosTokenType.LBRACKET, "[", startLine, startColumn, startOffset)
            ']' -> addToken(LogosTokenType.RBRACKET, "]", startLine, startColumn, startOffset)
            '&' -> addToken(LogosTokenType.AMPERSAND, "&", startLine, startColumn, startOffset)
            '=' -> addToken(LogosTokenType.EQUALS, "=", startLine, startColumn, startOffset)
            '*' -> addToken(LogosTokenType.ASTERISK, "*", startLine, startColumn, startOffset)
            '+', '-' -> {
                // Could be method scope or operator
                if (isMethodScopeContext()) {
                    addToken(LogosTokenType.OBJ_C_METHOD_SCOPE, c.toString(), startLine, startColumn, startOffset)
                } else {
                    // Regular operator - tokenize to ensure complete coverage
                    addToken(LogosTokenType.ERROR, c.toString(), startLine, startColumn, startOffset)
                }
            }
            '%' -> {
                // Logos directive
                val directive = scanDirective()
                val directiveType = when (directive) {
                    "hook" -> LogosTokenType.DIRECTIVE_HOOK
                    "subclass" -> LogosTokenType.DIRECTIVE_SUBCLASS
                    "group" -> LogosTokenType.DIRECTIVE_GROUP
                    "class" -> LogosTokenType.DIRECTIVE_CLASS
                    "c" -> LogosTokenType.DIRECTIVE_C
                    "new" -> LogosTokenType.DIRECTIVE_NEW
                    "orig" -> LogosTokenType.DIRECTIVE_ORIG
                    "log" -> LogosTokenType.DIRECTIVE_LOG
                    "ctor" -> LogosTokenType.DIRECTIVE_CTOR
                    "dtor" -> LogosTokenType.DIRECTIVE_DTOR
                    "init" -> LogosTokenType.DIRECTIVE_INIT
                    "end" -> LogosTokenType.DIRECTIVE_END
                    "config" -> LogosTokenType.DIRECTIVE_CONFIG
                    "property" -> LogosTokenType.DIRECTIVE_PROPERTY
                    "hookf" -> LogosTokenType.DIRECTIVE_HOOKF
                    else -> LogosTokenType.ERROR
                }
                addToken(directiveType, "%$directive", startLine, startColumn, startOffset)
            }
            '@' -> {
                // Objective-C keyword
                // Scan the keyword part AFTER the @ (don't include @ in scan)
                val start = position
                while (isAlphaNumeric(peek()) || peek() == '_') {
                    advance()
                }
                val keyword = source.substring(start, position)
                val keywordType = when (keyword) {
                    "interface" -> LogosTokenType.OBJ_C_INTERFACE
                    "implementation" -> LogosTokenType.OBJ_C_IMPLEMENTATION
                    "protocol" -> LogosTokenType.OBJ_C_PROTOCOL
                    "end" -> LogosTokenType.OBJ_C_END
                    "selector" -> LogosTokenType.OBJ_C_SELECTOR
                    "property" -> LogosTokenType.OBJ_C_PROPERTY
                    else -> LogosTokenType.IDENTIFIER
                }
                addToken(keywordType, "@$keyword", startLine, startColumn, startOffset)
            }
            '"' -> {
                // String literal
                val str = scanString()
                addToken(LogosTokenType.STRING, str, startLine, startColumn, startOffset)
            }
            '.' -> {
                // Check for ellipsis
                if (peek() == '.' && peekNext() == '.') {
                    advance() // second .
                    advance() // third .
                    addToken(LogosTokenType.ELLIPSIS, "...", startLine, startColumn, startOffset)
                } else {
                    // Single dot or double dot - tokenize as error
                    addToken(LogosTokenType.ERROR, ".", startLine, startColumn, startOffset)
                }
            }
            else -> {
                if (isDigit(c)) {
                    val number = scanNumber()
                    addToken(LogosTokenType.NUMBER, number, startLine, startColumn, startOffset)
                } else if (isAlpha(c) || c == '_' || c == '$') {
                    val identifier = scanIdentifier()
                    addToken(LogosTokenType.IDENTIFIER, identifier, startLine, startColumn, startOffset)
                } else {
                    // Unknown character - must tokenize to ensure complete coverage
                    addToken(LogosTokenType.ERROR, c.toString(), startLine, startColumn, startOffset)
                }
            }
        }
    }

    private fun scanDirective(): String {
        val start = position
        while (isAlpha(peek()) || peek() == '_') {
            advance()
        }
        return source.substring(start, position)
    }

    private fun scanIdentifier(): String {
        val start = position - 1
        while (isAlphaNumeric(peek()) || peek() == '_' || peek() == '$' || peek() == '.') {
            advance()
        }
        return source.substring(start, position)
    }

    private fun scanNumber(): String {
        val start = position - 1
        while (isDigit(peek())) advance()

        if (peek() == '.' && isDigit(peekNext())) {
            advance() // consume '.'
            while (isDigit(peek())) advance()
        }

        return source.substring(start, position)
    }

    private fun scanString(): String {
        val start = position - 1
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\\') {
                advance() // Skip escape character
                advance() // Skip escaped character
            } else {
                if (advance() == '\n') {
                    line++
                    column = 0
                }
            }
        }

        if (!isAtEnd()) advance() // Closing "

        return source.substring(start, position)
    }

    private fun isMethodScopeContext(): Boolean {
        // Simple heuristic: if followed by whitespace and then '(', it's likely a method scope
        var i = position
        while (i < source.length && (source[i] == ' ' || source[i] == '\t')) i++
        return i < source.length && source[i] == '('
    }

    private fun isDigit(c: Char): Boolean = c in '0'..'9'

    private fun isAlpha(c: Char): Boolean = c in 'a'..'z' || c in 'A'..'Z'

    private fun isAlphaNumeric(c: Char): Boolean = isAlpha(c) || isDigit(c)

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[position] != expected) return false

        position++
        column++
        return true
    }

    private fun peek(): Char {
        if (isAtEnd()) return '\u0000'
        return source[position]
    }

    private fun peekNext(): Char {
        if (position + 1 >= source.length) return '\u0000'
        return source[position + 1]
    }

    private fun advance(): Char {
        val c = source[position]
        position++
        column++
        return c
    }

    private fun isAtEnd(): Boolean = position >= source.length

    private fun addToken(type: LogosTokenType, text: String, line: Int, column: Int, offset: Int) {
        // Calculate actual length based on current position
        val actualLength = position - offset
        tokens.add(LogosToken(type, text, line, column, offset, actualLength))
    }
}
