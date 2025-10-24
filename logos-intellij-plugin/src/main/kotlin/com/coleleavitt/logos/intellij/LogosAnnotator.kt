package com.coleleavitt.logos.intellij

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.util.PsiTreeUtil

/**
 * Annotator for semantic highlighting and error detection in Logos files.
 *
 * Provides:
 * - Error highlighting for unmatched %hook/%end pairs
 * - Warning for %orig used outside of hooked methods
 * - Error for %new used outside of hook/subclass blocks
 * - Warning for invalid directive usage patterns
 */
class LogosAnnotator : Annotator {

    companion object {
        private val LOG = Logger.getInstance(LogosAnnotator::class.java)
    }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is LogosPsiElement) {
            return
        }

        val node = element.node
        val elementType = node.elementType

        // Check for bad characters (lexer-level errors)
        if (elementType == TokenType.BAD_CHARACTER) {
            val text = element.text
            holder.newAnnotation(
                HighlightSeverity.ERROR,
                "Invalid character or syntax: '$text'"
            )
                .range(element.textRange)
                .create()

            LOG.debug("Found bad character '$text' at ${element.textRange}")
            return
        }

        when (elementType) {
            LogosElementTypes.DIRECTIVE_ORIG,
            LogosElementTypes.DIRECTIVE_ORIG_PTR -> {
                checkOrigDirective(element, holder)
            }

            LogosElementTypes.DIRECTIVE_NEW -> {
                checkNewDirective(element, holder)
            }

            LogosElementTypes.DIRECTIVE_END -> {
                checkEndDirective(element, holder)
            }

            LogosElementTypes.DIRECTIVE_INIT -> {
                checkInitDirective(element, holder)
            }

            LogosElementTypes.DIRECTIVE_C -> {
                checkCDirective(element, holder)
            }

            LogosElementTypes.DIRECTIVE_HOOK -> {
                checkHookDirective(element, holder)
            }

            LogosElementTypes.DIRECTIVE_SUBCLASS -> {
                checkSubclassDirective(element, holder)
            }

            LogosElementTypes.DIRECTIVE_GROUP -> {
                checkGroupDirective(element, holder)
            }

            LogosElementTypes.DIRECTIVE_PROPERTY -> {
                checkPropertyDirective(element, holder)
            }

            LogosElementTypes.DIRECTIVE_HOOKF -> {
                checkHookfDirective(element, holder)
            }

            LogosElementTypes.DIRECTIVE_LOG -> {
                checkLogDirective(element, holder)
            }
        }
    }

    /**
     * Check if %orig or &%orig is used inside a hooked method.
     * %orig and &%orig should only appear inside method implementations within %hook or %subclass blocks.
     */
    private fun checkOrigDirective(element: PsiElement, holder: AnnotationHolder) {
        if (!isInsideHookOrSubclass(element)) {
            val directive = if (element.text.startsWith("&")) "&%orig" else "%orig"
            holder.newAnnotation(
                HighlightSeverity.ERROR,
                "$directive can only be used inside hooked methods"
            )
                .range(element.textRange)
                .create()

            LOG.debug("Found $directive outside of hook/subclass at ${element.textRange}")
        }
    }

    /**
     * Check if %new is used inside a hook or subclass block.
     * %new should only appear inside %hook or %subclass blocks.
     */
    private fun checkNewDirective(element: PsiElement, holder: AnnotationHolder) {
        if (!isInsideHookOrSubclass(element)) {
            holder.newAnnotation(
                HighlightSeverity.ERROR,
                "%new can only be used inside %hook or %subclass blocks"
            )
                .range(element.textRange)
                .create()

            LOG.debug("Found %new outside of hook/subclass at ${element.textRange}")
        }
    }

    /**
     * Check if %end has a matching opening directive.
     * Looks backwards to find %hook, %subclass, or %group.
     */
    private fun checkEndDirective(element: PsiElement, holder: AnnotationHolder) {
        if (!hasMatchingOpeningDirective(element)) {
            holder.newAnnotation(
                HighlightSeverity.ERROR,
                "Unmatched %end directive - no corresponding %hook, %subclass, or %group found"
            )
                .range(element.textRange)
                .create()

            LOG.debug("Found unmatched %end at ${element.textRange}")
        }
    }

    /**
     * Check if %init is used properly.
     * Warn if %init is used inside a hook block (should be in %ctor usually).
     */
    private fun checkInitDirective(element: PsiElement, holder: AnnotationHolder) {
        if (isInsideHookOrSubclass(element)) {
            holder.newAnnotation(
                HighlightSeverity.WEAK_WARNING,
                "%init is typically used in %ctor, not inside hook blocks"
            )
                .range(element.textRange)
                .create()

            LOG.debug("Found %init inside hook/subclass at ${element.textRange}")
        }
    }

    /**
     * Check if %c directive has proper syntax.
     * Should be followed by parentheses with a class name.
     */
    private fun checkCDirective(element: PsiElement, holder: AnnotationHolder) {
        val nextElement = PsiTreeUtil.skipWhitespacesForward(element)
        if (nextElement == null || (nextElement as? LogosPsiElement)?.node?.elementType != LogosElementTypes.LPAREN) {
            holder.newAnnotation(
                HighlightSeverity.ERROR,
                "%c must be followed by (ClassName)"
            )
                .range(element.textRange)
                .create()

            LOG.debug("Found %c without parentheses at ${element.textRange}")
        }
    }

    /**
     * Check if element is inside a %hook or %subclass block.
     * Walks up the PSI tree looking for opening directives before finding %end.
     */
    private fun isInsideHookOrSubclass(element: PsiElement): Boolean {
        var current: PsiElement? = element.prevSibling ?: element.parent?.prevSibling
        var depth = 0

        while (current != null) {
            if (current is LogosPsiElement) {
                when (current.node.elementType) {
                    LogosElementTypes.DIRECTIVE_END -> depth++
                    LogosElementTypes.DIRECTIVE_HOOK,
                    LogosElementTypes.DIRECTIVE_SUBCLASS -> {
                        if (depth == 0) {
                            return true
                        }
                        depth--
                    }
                }
            }
            current = current.prevSibling ?: current.parent?.prevSibling
        }

        return false
    }

    /**
     * Check if %end has a matching opening directive.
     * Looks backwards for %hook, %subclass, or %group.
     */
    private fun hasMatchingOpeningDirective(element: PsiElement): Boolean {
        var current: PsiElement? = element.prevSibling ?: element.parent?.prevSibling
        var depth = 0

        while (current != null) {
            if (current is LogosPsiElement) {
                when (current.node.elementType) {
                    LogosElementTypes.DIRECTIVE_END -> depth++
                    LogosElementTypes.DIRECTIVE_HOOK,
                    LogosElementTypes.DIRECTIVE_SUBCLASS,
                    LogosElementTypes.DIRECTIVE_GROUP -> {
                        if (depth == 0) {
                            return true
                        }
                        depth--
                    }
                }
            }
            current = current.prevSibling ?: current.parent?.prevSibling
        }

        return false
    }

    /**
     * Check if %hook has a class name following it.
     */
    private fun checkHookDirective(element: PsiElement, holder: AnnotationHolder) {
        val nextElement = PsiTreeUtil.skipWhitespacesAndCommentsForward(element)
        if (nextElement == null || (nextElement as? LogosPsiElement)?.node?.elementType != LogosElementTypes.IDENTIFIER) {
            holder.newAnnotation(
                HighlightSeverity.ERROR,
                "%hook must be followed by a class name"
            )
                .range(element.textRange)
                .create()

            LOG.debug("Found %hook without class name at ${element.textRange}")
        }
    }

    /**
     * Check if %subclass has proper syntax: %subclass NewClass : SuperClass
     */
    private fun checkSubclassDirective(element: PsiElement, holder: AnnotationHolder) {
        val nextElement = PsiTreeUtil.skipWhitespacesAndCommentsForward(element)
        if (nextElement == null || (nextElement as? LogosPsiElement)?.node?.elementType != LogosElementTypes.IDENTIFIER) {
            holder.newAnnotation(
                HighlightSeverity.ERROR,
                "%subclass must be followed by: NewClassName : SuperClassName"
            )
                .range(element.textRange)
                .create()

            LOG.debug("Found %subclass without class name at ${element.textRange}")
        }
    }

    /**
     * Check if %group has a group name following it.
     */
    private fun checkGroupDirective(element: PsiElement, holder: AnnotationHolder) {
        val nextElement = PsiTreeUtil.skipWhitespacesAndCommentsForward(element)
        if (nextElement == null || (nextElement as? LogosPsiElement)?.node?.elementType != LogosElementTypes.IDENTIFIER) {
            holder.newAnnotation(
                HighlightSeverity.ERROR,
                "%group must be followed by a group name"
            )
                .range(element.textRange)
                .create()

            LOG.debug("Found %group without group name at ${element.textRange}")
        }
    }

    /**
     * Check if %property is used inside a hook or subclass block.
     */
    private fun checkPropertyDirective(element: PsiElement, holder: AnnotationHolder) {
        if (!isInsideHookOrSubclass(element)) {
            holder.newAnnotation(
                HighlightSeverity.ERROR,
                "%property can only be used inside %hook or %subclass blocks"
            )
                .range(element.textRange)
                .create()

            LOG.debug("Found %property outside of hook/subclass at ${element.textRange}")
        }
    }

    /**
     * Check if %hookf has proper function signature.
     * Should be: %hookf(returnType, functionName, args...)
     */
    private fun checkHookfDirective(element: PsiElement, holder: AnnotationHolder) {
        val nextElement = PsiTreeUtil.skipWhitespacesForward(element)
        if (nextElement == null || (nextElement as? LogosPsiElement)?.node?.elementType != LogosElementTypes.LPAREN) {
            holder.newAnnotation(
                HighlightSeverity.ERROR,
                "%hookf must be followed by (returnType, functionName, args...)"
            )
                .range(element.textRange)
                .create()

            LOG.debug("Found %hookf without parentheses at ${element.textRange}")
        }
    }

    /**
     * Check if %log is used inside a hooked method.
     */
    private fun checkLogDirective(element: PsiElement, holder: AnnotationHolder) {
        if (!isInsideHookOrSubclass(element)) {
            holder.newAnnotation(
                HighlightSeverity.WARNING,
                "%log is typically used inside hooked methods"
            )
                .range(element.textRange)
                .create()

            LOG.debug("Found %log outside of hook/subclass at ${element.textRange}")
        }
    }
}
