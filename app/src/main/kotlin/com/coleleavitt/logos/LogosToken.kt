package com.coleleavitt.logos

/**
 * Token types for Logos language
 */
enum class LogosTokenType {
    // Directives
    DIRECTIVE_HOOK,           // %hook
    DIRECTIVE_SUBCLASS,       // %subclass
    DIRECTIVE_GROUP,          // %group
    DIRECTIVE_CLASS,          // %class (deprecated)
    DIRECTIVE_C,              // %c()
    DIRECTIVE_NEW,            // %new
    DIRECTIVE_ORIG,           // %orig
    DIRECTIVE_LOG,            // %log
    DIRECTIVE_CTOR,           // %ctor
    DIRECTIVE_DTOR,           // %dtor
    DIRECTIVE_INIT,           // %init
    DIRECTIVE_END,            // %end
    DIRECTIVE_CONFIG,         // %config
    DIRECTIVE_PROPERTY,       // %property
    DIRECTIVE_HOOKF,          // %hookf

    // Objective-C Keywords
    OBJ_C_METHOD_SCOPE,       // + or -
    OBJ_C_INTERFACE,          // @interface
    OBJ_C_IMPLEMENTATION,     // @implementation
    OBJ_C_PROTOCOL,           // @protocol
    OBJ_C_END,                // @end
    OBJ_C_SELECTOR,           // @selector
    OBJ_C_PROPERTY,           // @property

    // Literals and Identifiers
    IDENTIFIER,               // Variable/class/method names
    SWIFT_CLASS_NAME,         // Swift.Class.Name (with dots)
    NUMBER,                   // Numeric literals
    STRING,                   // String literals

    // Operators and Punctuation
    COLON,                    // :
    SEMICOLON,                // ;
    COMMA,                    // ,
    LPAREN,                   // (
    RPAREN,                   // )
    LBRACE,                   // {
    RBRACE,                   // }
    LANGLE,                   // <
    RANGLE,                   // >
    LBRACKET,                 // [
    RBRACKET,                 // ]
    AMPERSAND,                // &
    ELLIPSIS,                 // ...
    EQUALS,                   // =
    ASTERISK,                 // *
    SLASH,                    // /

    // Comments and Whitespace
    COMMENT,
    WHITESPACE,
    NEWLINE,

    // Special
    EOF,
    ERROR
}

/**
 * Represents a token in the Logos source
 */
data class LogosToken(
    val type: LogosTokenType,
    val text: String,
    val line: Int,
    val column: Int,
    val offset: Int = 0,  // Absolute offset in source
    val length: Int = text.length
) {
    val endColumn: Int get() = column + length
    val endOffset: Int get() = offset + length
}

/**
 * Position in source code
 */
data class SourcePosition(
    val line: Int,
    val column: Int,
    val offset: Int
)

/**
 * Range in source code
 */
data class SourceRange(
    val start: SourcePosition,
    val end: SourcePosition
)
