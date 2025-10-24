package com.coleleavitt.logos.intellij

import com.intellij.lang.Language

/**
 * Logos Language definition for IntelliJ Platform
 *
 * Logos is a preprocessor for Objective-C that provides elegant syntax for
 * method hooking and runtime class manipulation, used in Theos tweak development.
 */
class LogosLanguage private constructor() : Language("Logos") {
    companion object {
        @JvmStatic
        val INSTANCE = LogosLanguage()
    }

    override fun isCaseSensitive() = true

    override fun getDisplayName() = "Logos"
}
