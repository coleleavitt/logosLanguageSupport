package com.coleleavitt.logos.intellij

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

/**
 * Code folding builder for Logos files.
 *
 * Supports folding for:
 * - %hook...%end blocks
 * - %subclass...%end blocks
 * - %group...%end blocks
 * - %ctor { } blocks
 * - Method bodies { }
 * - Block comments
 */
class LogosFoldingBuilder : FoldingBuilderEx() {

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()

        // Add null safety check
        if (root.node == null) {
            return FoldingDescriptor.EMPTY_ARRAY
        }

        // Find all blocks that can be folded
        try {
            val children = PsiTreeUtil.findChildrenOfAnyType(root, LogosPsiElement::class.java)
            children.forEach { element ->
                if (element?.node != null) {
                    val range = getFoldingRange(element)
                    if (range != null && range.length > 1) {
                        descriptors.add(FoldingDescriptor(element.node, range))
                    }
                }
            }
        } catch (e: Exception) {
            // Safely handle any PSI traversal errors
            return FoldingDescriptor.EMPTY_ARRAY
        }

        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String? {
        val elementType = node.elementType

        return when (elementType) {
            LogosElementTypes.DIRECTIVE_HOOK -> "%hook..."
            LogosElementTypes.DIRECTIVE_SUBCLASS -> "%subclass..."
            LogosElementTypes.DIRECTIVE_GROUP -> "%group..."
            LogosElementTypes.DIRECTIVE_CTOR -> "%ctor {...}"
            LogosElementTypes.LBRACE -> "{...}"
            LogosElementTypes.COMMENT -> "/*...*/"
            else -> "..."
        }
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        // Don't collapse anything by default
        return false
    }

    private fun getFoldingRange(element: PsiElement): TextRange? {
        val node = element.node
        val elementType = node.elementType

        // For block directives, fold from directive to %end
        if (elementType in LogosParserDefinition.DIRECTIVES) {
            return element.textRange
        }

        // For braces, fold the contents
        if (elementType == LogosElementTypes.LBRACE) {
            return element.textRange
        }

        return null
    }
}
