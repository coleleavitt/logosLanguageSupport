package com.coleleavitt.logos.intellij

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement

/**
 * Annotator for semantic highlighting and error detection in Logos files.
 *
 * Provides:
 * - Error highlighting for unmatched %hook/%end pairs
 * - Warning for %orig used outside of hooked methods
 * - Info hints for deprecated patterns
 */
class LogosAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is LogosPsiElement) {
            return
        }

        val node = element.node
        val elementType = node.elementType

        // Example: Highlight potential errors
        when (elementType) {
            LogosElementTypes.DIRECTIVE_ORIG -> {
                // TODO: Check if %orig is inside a hook method
                // For now, just demonstrate the annotation API
            }

            LogosElementTypes.DIRECTIVE_NEW -> {
                // TODO: Check if %new is inside a hook/subclass block
            }

            LogosElementTypes.DIRECTIVE_END -> {
                // TODO: Check for matching opening directive
            }
        }
    }
}
