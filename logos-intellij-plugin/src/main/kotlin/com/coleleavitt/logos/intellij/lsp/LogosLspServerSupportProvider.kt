package com.coleleavitt.logos.intellij.lsp

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor
import com.intellij.platform.lsp.api.lsWidget.LspServerWidgetItem
import java.io.File

/**
 * LSP Server Support Provider for Logos Language
 *
 * Uses IntelliJ's native LSP API (available in RustRover and all commercial JetBrains IDEs)
 */
class LogosLspServerSupportProvider : LspServerSupportProvider {

    override fun fileOpened(
        project: Project,
        file: VirtualFile,
        serverStarter: LspServerSupportProvider.LspServerStarter
    ) {
        // Start server for all Logos file extensions
        if (isLogosFile(file)) {
            serverStarter.ensureServerStarted(LogosLspServerDescriptor(project))
        }
    }

    override fun createLspServerWidgetItem(
        lspServer: com.intellij.platform.lsp.api.LspServer,
        currentFile: VirtualFile?
    ): LspServerWidgetItem {
        return LspServerWidgetItem(lspServer, currentFile)
    }

    private fun isLogosFile(file: VirtualFile): Boolean {
        return file.extension in setOf("x", "xm", "xi", "xmi")
    }
}

/**
 * Logos Language Server Descriptor
 *
 * Manages the lifecycle of the Logos Language Server process
 */
private class LogosLspServerDescriptor(project: Project)
    : ProjectWideLspServerDescriptor(project, "Logos Language Server") {

    override fun isSupportedFile(file: VirtualFile): Boolean {
        return file.extension in setOf("x", "xm", "xi", "xmi")
    }

    override fun createCommandLine(): GeneralCommandLine {
        val serverPath = getLanguageServerPath()

        val commandLine = GeneralCommandLine()
        commandLine.exePath = getJavaExecutable()
        commandLine.addParameter("-jar")
        commandLine.addParameter(serverPath)

        // Set working directory to project base path
        commandLine.workDirectory = File(project.basePath ?: ".")

        return commandLine
    }

    private fun getLanguageServerPath(): String {
        // Use IntelliJ's PluginManager to get the plugin path
        val pluginId = PluginId.getId("com.coleleavitt.logos")
        val plugin = PluginManager.getInstance().findEnabledPlugin(pluginId)
            ?: throw IllegalStateException("Logos plugin not found")

        // Get the plugin directory
        val pluginDir = plugin.pluginPath.toFile()
        val languageServerDir = File(pluginDir, "languageServer/lib")

        // Find the app JAR in the lib directory
        val appJar = languageServerDir.listFiles()?.find {
            it.name.startsWith("app") && it.name.endsWith(".jar")
        }

        return appJar?.absolutePath
            ?: throw IllegalStateException("Language server JAR not found in ${languageServerDir.absolutePath}")
    }

    private fun getJavaExecutable(): String {
        // Use the JVM that's running IntelliJ/RustRover
        val javaHome = System.getProperty("java.home")
        val javaBin = File(javaHome, "bin")
        val javaExe = File(javaBin, if (System.getProperty("os.name").lowercase().contains("win")) "java.exe" else "java")
        return javaExe.absolutePath
    }
}
