package com.coleleavitt.logos.intellij

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

/**
 * Find usages provider for Logos language.
 *
 * Enables "Find Usages" functionality for Logos symbols.
 */
class LogosFindUsagesProvider : FindUsagesProvider {

    override fun getWordsScanner(): WordsScanner {
        return DefaultWordsScanner(
            LogosLexerAdapter(),
            TokenSet.create(LogosElementTypes.IDENTIFIER),
            LogosParserDefinition.COMMENTS,
            LogosParserDefinition.STRINGS
        )
    }

    override fun canFindUsagesFor(psiElement: PsiElement): Boolean {
        return psiElement is LogosPsiElement
    }

    override fun getHelpId(psiElement: PsiElement): String? {
        return null
    }

    override fun getType(element: PsiElement): String {
        return "Logos symbol"
    }

    override fun getDescriptiveName(element: PsiElement): String {
        return element.text ?: "unknown"
    }

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
        return element.text ?: "unknown"
    }
}
