package com.coleleavitt.logos.intellij

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType

/**
 * Brace matching for Logos files.
 *
 * Matches:
 * - Curly braces { }
 * - Parentheses ( )
 * - Angle brackets < >
 */
class LogosBraceMatcher : PairedBraceMatcher {

    override fun getPairs(): Array<BracePair> {
        return PAIRS
    }

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean {
        return true
    }

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int {
        return openingBraceOffset
    }

}

private val PAIRS = arrayOf(
    BracePair(LogosElementTypes.LBRACE, LogosElementTypes.RBRACE, true),
    BracePair(LogosElementTypes.LPAREN, LogosElementTypes.RPAREN, false),
    BracePair(LogosElementTypes.LANGLE, LogosElementTypes.RANGLE, false)
)