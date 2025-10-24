package com.coleleavitt.logos.intellij.actions

import com.coleleavitt.logos.intellij.LogosFileType
import com.coleleavitt.logos.intellij.LogosIcons
import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory

/**
 * Action to create a new Logos file from template.
 *
 * Appears in File → New menu.
 */
class NewLogosFileAction : CreateFileFromTemplateAction(
    "Logos Tweak File",
    "Create a new Logos tweak file",
    LogosIcons.FILE
) {

    override fun buildDialog(
        project: Project,
        directory: PsiDirectory,
        builder: CreateFileFromTemplateDialog.Builder
    ) {
        builder
            .setTitle("New Logos File")
            .addKind("Tweak", LogosIcons.FILE, "Logos Tweak")
            .addKind("Hook", LogosIcons.FILE, "Logos Hook")
            .addKind("Subclass", LogosIcons.FILE, "Logos Subclass")
    }

    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String {
        return "Create Logos File $newName"
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is NewLogosFileAction
    }
}
