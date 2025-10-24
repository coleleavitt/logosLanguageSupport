package com.coleleavitt.logos.intellij

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

/**
 * File type definition for Logos files (.x, .xm, .xi, .xmi)
 */
class LogosFileType private constructor() : LanguageFileType(LogosLanguage.INSTANCE) {

    companion object {
        @JvmField
        val INSTANCE = LogosFileType()
    }

    override fun getName() = "Logos"

    override fun getDescription() = "Logos tweak file"

    override fun getDefaultExtension() = "x"

    override fun getIcon(): Icon? = LogosIcons.FILE

    override fun getDisplayName() = "Logos"
}

/**
 * Icon provider for Logos files
 */
object LogosIcons {
    // TODO: Replace with actual icon
    val FILE: Icon = com.intellij.openapi.util.IconLoader.getIcon(
        "/icons/logos.svg",
        LogosIcons::class.java
    )
}
