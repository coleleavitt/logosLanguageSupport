package com.coleleavitt.logos.intellij

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

/**
 * Parser definition for Logos language.
 *
 * This uses a lexer-only approach (no full AST parsing) which is sufficient
 * for syntax highlighting, code completion, and basic language features.
 *
 * Based on JsonParserDefinition from IntelliJ Platform.
 */
class LogosParserDefinition : ParserDefinition {

    override fun createLexer(project: Project?): Lexer {
        return LogosLexerAdapter()
    }

    override fun createParser(project: Project?): PsiParser {
        // Simple parser that creates a flat structure - just wraps the lexer tokens
        return LogosParser()
    }

    override fun getFileNodeType(): IFileElementType {
        return FILE
    }

    override fun getCommentTokens(): TokenSet {
        return COMMENTS
    }

    override fun getStringLiteralElements(): TokenSet {
        return STRINGS
    }

    override fun createElement(astNode: ASTNode): PsiElement {
        return LogosPsiElement(astNode)
    }

    override fun createFile(fileViewProvider: FileViewProvider): PsiFile {
        return LogosFile(fileViewProvider)
    }

    override fun spaceExistenceTypeBetweenTokens(left: ASTNode?, right: ASTNode?): ParserDefinition.SpaceRequirements {
        return ParserDefinition.SpaceRequirements.MAY
    }

    companion object {
        val FILE = IFileElementType(LogosLanguage.INSTANCE)

        val COMMENTS = TokenSet.create(LogosElementTypes.COMMENT)

        val STRINGS = TokenSet.create(LogosElementTypes.STRING)

        /**
         * All Logos directive tokens
         */
        val DIRECTIVES = TokenSet.create(
            LogosElementTypes.DIRECTIVE_HOOK,
            LogosElementTypes.DIRECTIVE_SUBCLASS,
            LogosElementTypes.DIRECTIVE_GROUP,
            LogosElementTypes.DIRECTIVE_NEW,
            LogosElementTypes.DIRECTIVE_ORIG,
            LogosElementTypes.DIRECTIVE_LOG,
            LogosElementTypes.DIRECTIVE_CTOR,
            LogosElementTypes.DIRECTIVE_DTOR,
            LogosElementTypes.DIRECTIVE_INIT,
            LogosElementTypes.DIRECTIVE_END,
            LogosElementTypes.DIRECTIVE_CONFIG,
            LogosElementTypes.DIRECTIVE_PROPERTY,
            LogosElementTypes.DIRECTIVE_HOOKF,
            LogosElementTypes.DIRECTIVE_C
        )
    }
}
