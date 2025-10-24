package com.coleleavitt.logos.intellij

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType

/**
 * Simple parser for Logos language that creates a flat AST structure.
 *
 * This parser doesn't build a complex tree - it just consumes all tokens
 * and creates a flat file structure. This is sufficient for syntax highlighting,
 * code completion, and basic language features.
 */
class LogosParser : PsiParser {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val marker = builder.mark()

        // Simply consume all tokens without building a complex tree
        while (!builder.eof()) {
            builder.advanceLexer()
        }

        marker.done(root)
        return builder.treeBuilt
    }
}
