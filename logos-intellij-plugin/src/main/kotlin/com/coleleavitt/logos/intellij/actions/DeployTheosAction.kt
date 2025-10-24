package com.coleleavitt.logos.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent

/**
 * Action to deploy a Theos project to a device.
 *
 * Runs 'make package install' in the project root directory.
 */
class DeployTheosAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        try {
            deployTheos(project)
        } catch (ex: Exception) {
            Messages.showErrorDialog(
                project,
                "Failed to deploy Theos project: ${ex.message}",
                "Deploy Error"
            )
        }
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        e.presentation.isEnabled = project != null && hasMakefile(project)
    }

    private fun deployTheos(project: Project) {
        val basePath = project.basePath ?: return

        val commandLine = GeneralCommandLine()
            .withWorkDirectory(basePath)
            .withExePath("make")
            .withParameters("package", "install")

        val processHandler = OSProcessHandler(commandLine)

        processHandler.addProcessListener(object : ProcessAdapter() {
            override fun processTerminated(event: ProcessEvent) {
                val exitCode = event.exitCode
                if (exitCode == 0) {
                    Messages.showInfoMessage(
                        project,
                        "Theos deployment completed successfully!",
                        "Deploy Success"
                    )
                } else {
                    Messages.showErrorDialog(
                        project,
                        "Deploy failed with exit code $exitCode",
                        "Deploy Failed"
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
