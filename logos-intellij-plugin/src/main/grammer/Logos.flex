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

// Objective-C @ Literals
OBJ_C_STRING = "@" \" ([^\\\"\r\n] | \\[^\r\n] )* \"?
OBJ_C_SELECTOR = "@selector"
OBJ_C_ENCODE = "@encode"
OBJ_C_PROTOCOL = "@protocol"
OBJ_C_SYNCHRONIZED = "@synchronized"
OBJ_C_AUTORELEASEPOOL = "@autoreleasepool"
OBJ_C_TRY = "@try"
OBJ_C_CATCH = "@catch"
OBJ_C_FINALLY = "@finally"
OBJ_C_THROW = "@throw"
OBJ_C_AVAILABLE = "@available"

// Objective-C Keywords
OBJ_C_KEYWORDS = "interface" | "implementation" | "protocol" | "end" | "property" | "synthesize" | "dynamic" | "optional" | "required" | "class" | "public" | "private" | "protected" | "package" | "IBOutlet" | "IBAction" | "nonatomic" | "atomic" | "strong" | "weak" | "copy" | "assign" | "retain" | "readonly" | "readwrite" | "getter" | "setter"

// C/Objective-C Type Keywords
TYPE_KEYWORDS = "void" | "int" | "float" | "double" | "char" | "short" | "long" | "unsigned" | "signed" | "BOOL" | "id" | "Class" | "SEL" | "IMP" | "instancetype" | "NSInteger" | "NSUInteger" | "CGFloat" | "NSString" | "NSArray" | "NSDictionary" | "NSObject" | "NSLog"

// C Keywords
C_KEYWORDS = "return" | "if" | "else" | "for" | "while" | "do" | "switch" | "case" | "default" | "break" | "continue" | "sizeof" | "typedef" | "struct" | "union" | "enum" | "const" | "static" | "extern" | "inline" | "register" | "volatile" | "auto"

// Boolean/Null literals
LITERALS = "nil" | "Nil" | "NULL" | "YES" | "NO" | "TRUE" | "FALSE" | "true" | "false" | "self" | "super"

// Objective-C Method Scope
OBJ_C_METHOD_SCOPE = "+" | "-"

%%

<YYINITIAL> {
    // Logos Directives (must come before generic % pattern)
    "%hook"         { return LogosElementTypes.DIRECTIVE_HOOK; }
    "%subclass"     { return LogosElementTypes.DIRECTIVE_SUBCLASS; }
    "%group"        { return LogosElementTypes.DIRECTIVE_GROUP; }
    "%new"          { return LogosElementTypes.DIRECTIVE_NEW; }
    "&" [ \t]* "%orig"  { return LogosElementTypes.DIRECTIVE_ORIG_PTR; }
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

    // Invalid directive (catch %foo, %bar, etc.)
    "%"[a-zA-Z_]+   { return TokenType.BAD_CHARACTER; }

    // Comments
    {LINE_COMMENT}      { return LogosElementTypes.COMMENT; }
    {BLOCK_COMMENT}     { return LogosElementTypes.COMMENT; }

    // Objective-C @ Literals (must come before strings)
    {OBJ_C_STRING}      { return LogosElementTypes.OBJ_C_STRING; }
    {OBJ_C_SELECTOR}    { return LogosElementTypes.OBJ_C_AT_KEYWORD; }
    {OBJ_C_ENCODE}      { return LogosElementTypes.OBJ_C_AT_KEYWORD; }
    {OBJ_C_PROTOCOL}    { return LogosElementTypes.OBJ_C_AT_KEYWORD; }
    {OBJ_C_SYNCHRONIZED} { return LogosElementTypes.OBJ_C_AT_KEYWORD; }
    {OBJ_C_AUTORELEASEPOOL} { return LogosElementTypes.OBJ_C_AT_KEYWORD; }
    {OBJ_C_TRY}         { return LogosElementTypes.OBJ_C_AT_KEYWORD; }
    {OBJ_C_CATCH}       { return LogosElementTypes.OBJ_C_AT_KEYWORD; }
    {OBJ_C_FINALLY}     { return LogosElementTypes.OBJ_C_AT_KEYWORD; }
    {OBJ_C_THROW}       { return LogosElementTypes.OBJ_C_AT_KEYWORD; }
    {OBJ_C_AVAILABLE}   { return LogosElementTypes.OBJ_C_AT_KEYWORD; }

    // Objective-C Keywords
    {OBJ_C_KEYWORDS}    { return LogosElementTypes.OBJ_C_KEYWORD; }

    // Type Keywords
    {TYPE_KEYWORDS}     { return LogosElementTypes.TYPE_KEYWORD; }

    // C Keywords
    {C_KEYWORDS}        { return LogosElementTypes.C_KEYWORD; }

    // Boolean/Null Literals
    {LITERALS}          { return LogosElementTypes.LITERAL; }

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
