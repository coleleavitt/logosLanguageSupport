package com.coleleavitt.logos.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType

/**
 * Action to build a Theos project.
 *
 * Runs 'make' in the project root directory.
 */
class BuildTheosAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        try {
            buildTheos(project)
        } catch (ex: Exception) {
            Messages.showErrorDialog(
                project,
                "Failed to build Theos project: ${ex.message}",
                "Build Error"
            )
        }
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        e.presentation.isEnabled = project != null && hasMakefile(project)
    }

    private fun buildTheos(project: Project) {
        val basePath = project.basePath ?: return

        val commandLine = GeneralCommandLine()
            .withWorkDirectory(basePath)
            .withExePath("make")

        val processHandler = OSProcessHandler(commandLine)

        processHandler.addProcessListener(object : ProcessAdapter() {
            override fun processTerminated(event: ProcessEvent) {
                val exitCode = event.exitCode
                if (exitCode == 0) {
                    Messages.showInfoMessage(
                        project,
                        "Theos build completed successfully!",
                        "Build Success"
                    )
                } else {
                    Messages.showErrorDialog(
                        project,
                        "Build failed with exit code $exitCode",
                        "Build Failed"
                    )
                }
            }
        })

        processHandler.startNotify()
    }

    private fun hasMakefile(project: Project): Boolean {
        val basePath = project.basePath ?: return false
        val makefile = java.io.File(basePath, "Makefile")
        return makefile.exists()
    }
}
