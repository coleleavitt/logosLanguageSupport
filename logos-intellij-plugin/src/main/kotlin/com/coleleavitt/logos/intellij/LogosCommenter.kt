package com.coleleavitt.logos.intellij

import com.intellij.lang.Commenter

/**
 * Commenter support for Logos files.
 *
 * Logos uses C-style comments:
 * - Line comments: // comment
 * - Block comments: slash-star comment star-slash
 */
class LogosCommenter : Commenter {

    override fun getLineCommentPrefix(): String {
        return "//"
    }

    override fun getBlockCommentPrefix(): String {
        return "/*"
    }

    override fun getBlockCommentSuffix(): String {
        return "*/"
    }

    override fun getCommentedBlockCommentPrefix(): String? {
        return null
    }

    override fun getCommentedBlockCommentSuffix(): String? {
        return null
    }
}
