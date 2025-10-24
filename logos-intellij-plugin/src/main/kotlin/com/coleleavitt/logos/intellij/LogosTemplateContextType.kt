package com.coleleavitt.logos.intellij

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType

/**
 * Live template context for Logos files.
 *
 * Enables live templates (code snippets) to work in Logos files.
 */
class LogosTemplateContextType : TemplateContextType("Logos") {

    override fun isInContext(templateActionContext: TemplateActionContext): Boolean {
        return templateActionContext.file is LogosFile
    }

    override fun getPresentableName(): String {
        return "Logos"
    }
}
