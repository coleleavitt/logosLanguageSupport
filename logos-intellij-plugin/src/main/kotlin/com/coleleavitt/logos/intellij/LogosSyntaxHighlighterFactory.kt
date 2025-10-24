package com.coleleavitt.logos.intellij

import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

/**
 * Factory for creating Logos syntax highlighters.
 *
 * This is registered in plugin.xml and provides syntax highlighter instances
 * for Logos files.
 */
class LogosSyntaxHighlighterFactory : SyntaxHighlighterFactory() {

    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter {
        return LogosSyntaxHighlighter()
    }
}
