package com.coleleavitt.logos.intellij

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext

/**
 * Completion contributor for Logos language.
 *
 * Provides code completion for:
 * - Logos directives (%hook, %subclass, etc.)
 * - Common Objective-C classes
 * - Property attributes
 */
class LogosCompletionContributor : CompletionContributor() {

    init {
        // Completion for all Logos elements
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inFile(PlatformPatterns.psiFile(LogosFile::class.java)),
            LogosCompletionProvider()
        )
    }
}

/**
 * Completion provider implementation.
 */
class LogosCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        // Add Logos directive completions
        DIRECTIVE_COMPLETIONS.forEach { result.addElement(it) }

        // Add common Objective-C class completions
        OBJC_CLASS_COMPLETIONS.forEach { result.addElement(it) }
    }

    companion object {
        private val DIRECTIVE_COMPLETIONS = listOf(
            createDirectiveCompletion("%hook", $$"%hook ${1:ClassName}\n\t$0\n%end"),
            createDirectiveCompletion("%subclass", $$"%subclass ${1:NewClass} : ${2:ParentClass}\n\t$0\n%end"),
            createDirectiveCompletion("%group", $$"%group ${1:GroupName}\n\t$0\n%end"),
            createDirectiveCompletion("%new", $$"%new\n- (void)${1:methodName} {\n\t$0\n}"),
            createDirectiveCompletion("%orig", "%orig"),
            createDirectiveCompletion("&%orig", "&%orig"),
            createDirectiveCompletion("%log", "%log"),
            createDirectiveCompletion("%init", "%init"),
            createDirectiveCompletion("%ctor", "%ctor {\n\t$0\n}"),
            createDirectiveCompletion("%dtor", "%dtor {\n\t$0\n}"),
            createDirectiveCompletion(
                "%property",
                $$"%property (${1:nonatomic}, ${2:strong}) ${3:NSString} *${4:propertyName};"
            ),
            createDirectiveCompletion("%config", $$"%config(${1:option}=${2:value})"),
            createDirectiveCompletion("%hookf", $$"%hookf(${1:returnType}, ${2:functionName}, ${3:args}) {\n\t$0\n}"),
            createDirectiveCompletion("%c", $$"%c(${1:ClassName})"),
            createDirectiveCompletion("%end", "%end")
        )

        private val OBJC_CLASS_COMPLETIONS = listOf(
            "NSObject", "NSString", "NSArray", "NSDictionary", "NSSet",
            "UIView", "UIViewController", "UIButton", "UILabel", "UIImageView",
            "UITableView", "UITableViewController", "UICollectionView",
            "SpringBoard", "SBApplicationController", "SBIconController"
        ).map { LookupElementBuilder.create(it).withTypeText("Class") }

        private fun createDirectiveCompletion(label: String, insertText: String): LookupElementBuilder {
            return LookupElementBuilder.create(label)
                .withInsertHandler { context, _ ->
                    val document = context.document
                    var startOffset = context.startOffset
                    val tailOffset = context.tailOffset

                    // Check if there's a '%' before the completion that triggered it
                    if (startOffset > 0 && document.charsSequence[startOffset - 1] == '%') {
                        startOffset--  // Include the '%' in the replacement
                    }

                    // Replace with insertion text
                    document.deleteString(startOffset, tailOffset)
                    document.insertString(startOffset, insertText)

                    // Move cursor to first placeholder if it exists
                    val placeholderIndex = insertText.indexOf($$"${1")
                    if (placeholderIndex >= 0) {
                        context.editor.caretModel.moveToOffset(startOffset + placeholderIndex)
                    }
                }
                .withTypeText("Logos directive")
                .withBoldness(true)
        }
    }
}
