package com.coleleavitt.logos.intellij

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

/**
 * PSI file representation for Logos files (.x, .xm, .xi, .xmi)
 */
class LogosFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, LogosLanguage.INSTANCE) {

    override fun getFileType(): FileType {
        return LogosFileType.INSTANCE
    }

    override fun toString(): String {
        return "Logos File"
    }
}
