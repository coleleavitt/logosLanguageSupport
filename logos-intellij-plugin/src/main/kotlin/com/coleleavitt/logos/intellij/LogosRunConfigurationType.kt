package com.coleleavitt.logos.intellij

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import javax.swing.Icon

/**
 * Run configuration type for Logos/Theos projects.
 *
 * Allows creating run configurations for:
 * - Building with Theos (make)
 * - Deploying to device (make package install)
 * - Cleaning build artifacts (make clean)
 */
class LogosRunConfigurationType : ConfigurationType {

    override fun getDisplayName(): String {
        return "Theos Build"
    }

    override fun getConfigurationTypeDescription(): String {
        return "Build and deploy Theos tweaks"
    }

    override fun getIcon(): Icon {
        return LogosIcons.FILE
    }

    override fun getId(): String {
        return "LOGOS_RUN_CONFIGURATION"
    }

    override fun getConfigurationFactories(): Array<ConfigurationFactory> {
        return arrayOf(LogosConfigurationFactory(this))
    }
}

/**
 * Factory for creating Logos run configurations.
 */
class LogosConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {

    override fun getId(): String {
        return "Logos"
    }

    override fun createTemplateConfiguration(project: com.intellij.openapi.project.Project): com.intellij.execution.configurations.RunConfiguration {
        return LogosRunConfiguration(project, this, "Theos Build")
    }

    override fun getName(): String {
        return "Theos Build"
    }
}

/**
 * Run configuration for Theos builds.
 */
class LogosRunConfiguration(
    project: com.intellij.openapi.project.Project,
    factory: ConfigurationFactory,
    name: String
) : com.intellij.execution.configurations.RunConfigurationBase<com.intellij.execution.configurations.RunProfileState>(
    project,
    factory,
    name
) {
    // Configuration options
    var makeTarget: String = ""  // Empty = just "make", or "clean", "package", "install", etc.

    override fun getConfigurationEditor(): com.intellij.openapi.options.SettingsEditor<out com.intellij.execution.configurations.RunConfiguration> {
        return LogosRunConfigurationEditor()
    }

    override fun getState(
        executor: com.intellij.execution.Executor,
        environment: com.intellij.execution.runners.ExecutionEnvironment
    ): com.intellij.execution.configurations.RunProfileState? {
        return LogosRunProfileState(environment, makeTarget)
    }

    override fun readExternal(element: org.jdom.Element) {
        super.readExternal(element)
        makeTarget = element.getAttributeValue("makeTarget") ?: ""
    }

    override fun writeExternal(element: org.jdom.Element) {
        super.writeExternal(element)
        element.setAttribute("makeTarget", makeTarget)
    }
}

/**
 * Editor for Logos run configuration settings.
 */
class LogosRunConfigurationEditor : com.intellij.openapi.options.SettingsEditor<LogosRunConfiguration>() {

    private val panel = javax.swing.JPanel()
    private val makeTargetField = javax.swing.JTextField(20)

    init {
        panel.layout = java.awt.GridBagLayout()
        val gbc = java.awt.GridBagConstraints()
        gbc.insets = java.awt.Insets(5, 5, 5, 5)
        gbc.anchor = java.awt.GridBagConstraints.WEST

        // Label
        gbc.gridx = 0
        gbc.gridy = 0
        panel.add(javax.swing.JLabel("Make target:"), gbc)

        // Text field
        gbc.gridx = 1
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL
        gbc.weightx = 1.0
        panel.add(makeTargetField, gbc)

        // Help text
        gbc.gridx = 0
        gbc.gridy = 1
        gbc.gridwidth = 2
        panel.add(javax.swing.JLabel("Examples: (empty) = build, 'clean', 'package', 'install', 'package install'"), gbc)
    }

    override fun resetEditorFrom(configuration: LogosRunConfiguration) {
        makeTargetField.text = configuration.makeTarget
    }

    override fun applyEditorTo(configuration: LogosRunConfiguration) {
        configuration.makeTarget = makeTargetField.text.trim()
    }

    override fun createEditor(): javax.swing.JComponent {
        return panel
    }
}

/**
 * Run profile state for executing Theos builds.
 */
class LogosRunProfileState(
    private val environment: com.intellij.execution.runners.ExecutionEnvironment,
    private val makeTarget: String = ""
) : com.intellij.execution.configurations.RunProfileState {

    override fun execute(
        executor: com.intellij.execution.Executor?,
        runner: com.intellij.execution.runners.ProgramRunner<*>
    ): com.intellij.execution.ExecutionResult? {
        val project = environment.project
        val basePath = project.basePath ?: return null

        // Check if Makefile exists
        val makefile = java.io.File(basePath, "Makefile")
        if (!makefile.exists()) {
            return null
        }

        // Create command line for 'make' with optional target
        val commandLine = com.intellij.execution.configurations.GeneralCommandLine()
            .withWorkDirectory(basePath)
            .withExePath("make")

        // Add targets if specified (e.g., "clean", "package install")
        if (makeTarget.isNotBlank()) {
            commandLine.addParameters(makeTarget.split("\\s+".toRegex()))
        }

        // Create process handler
        val processHandler = com.intellij.execution.process.OSProcessHandler(commandLine)

        // Create console view to show output
        val consoleView = com.intellij.execution.filters.TextConsoleBuilderFactory
            .getInstance()
            .createBuilder(project)
            .console

        // Attach console to process
        consoleView.attachToProcess(processHandler)

        // Return execution result with console
        return com.intellij.execution.DefaultExecutionResult(
            consoleView,
            processHandler
        )
    }
}
