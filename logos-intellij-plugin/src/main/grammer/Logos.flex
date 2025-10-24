package com.coleleavitt.logos.intellij;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;

%%

%class LogosFlexLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{ return;
%eof}

// Whitespace
WHITE_SPACE = [ \t\r\n]+

// Comments
LINE_COMMENT = "//" [^\r\n]*
BLOCK_COMMENT = "/*" ( ([^"*"]|[\r\n])* ("*"+ [^"*""/"] )? )* ("*"+ "/")?

// Identifiers
IDENTIFIER = [a-zA-Z_][a-zA-Z0-9_]*

// Numbers
NUMBER = [0-9]+ | "0x" [0-9a-fA-F]+ | [0-9]+ "." [0-9]+ ([eE] [+-]? [0-9]+)?

// Strings
STRING = \" ([^\\\"\r\n] | \\[^\r\n] )* \"? | \' ([^\\\'\r\n] | \\[^\r\n] )* \'?

// Objective-C Method Scope
OBJ_C_METHOD_SCOPE = "+" | "-"

%%

<YYINITIAL> {
    // Logos Directives
    "%hook"         { return LogosElementTypes.DIRECTIVE_HOOK; }
    "%subclass"     { return LogosElementTypes.DIRECTIVE_SUBCLASS; }
    "%group"        { return LogosElementTypes.DIRECTIVE_GROUP; }
    "%new"          { return LogosElementTypes.DIRECTIVE_NEW; }
    "%orig"         { return LogosElementTypes.DIRECTIVE_ORIG; }
    "%log"          { return LogosElementTypes.DIRECTIVE_LOG; }
    "%ctor"         { return LogosElementTypes.DIRECTIVE_CTOR; }
    "%dtor"         { return LogosElementTypes.DIRECTIVE_DTOR; }
    "%init"         { return LogosElementTypes.DIRECTIVE_INIT; }
    "%end"          { return LogosElementTypes.DIRECTIVE_END; }
    "%config"       { return LogosElementTypes.DIRECTIVE_CONFIG; }
    "%property"     { return LogosElementTypes.DIRECTIVE_PROPERTY; }
    "%hookf"        { return LogosElementTypes.DIRECTIVE_HOOKF; }
    "%c"            { return LogosElementTypes.DIRECTIVE_C; }

    // Comments
    {LINE_COMMENT}      { return LogosElementTypes.COMMENT; }
    {BLOCK_COMMENT}     { return LogosElementTypes.COMMENT; }

    // Method Scope
    {OBJ_C_METHOD_SCOPE} { return LogosElementTypes.OBJ_C_METHOD_SCOPE; }

    // Punctuation
    "{"             { return LogosElementTypes.LBRACE; }
    "}"             { return LogosElementTypes.RBRACE; }
    "("             { return LogosElementTypes.LPAREN; }
    ")"             { return LogosElementTypes.RPAREN; }
    "<"             { return LogosElementTypes.LANGLE; }
    ">"             { return LogosElementTypes.RANGLE; }
    "["             { return LogosElementTypes.LBRACKET; }
    "]"             { return LogosElementTypes.RBRACKET; }
    ";"             { return LogosElementTypes.SEMICOLON; }
    ":"             { return LogosElementTypes.COLON; }
    ","             { return LogosElementTypes.COMMA; }
    "/"             { return LogosElementTypes.SLASH; }

    // Literals
    {STRING}        { return LogosElementTypes.STRING; }
    {NUMBER}        { return LogosElementTypes.NUMBER; }

    // Identifiers
    {IDENTIFIER}    { return LogosElementTypes.IDENTIFIER; }

    // Whitespace
    {WHITE_SPACE}   { return TokenType.WHITE_SPACE; }

    // Error
    [^]             { return TokenType.BAD_CHARACTER; }
}
