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
) : com.intellij.execution.configurations.RunConfigurationBase<com.intellij.execution.configurations.RunProfileState>(project, factory, name) {

    override fun getConfigurationEditor(): com.intellij.openapi.options.SettingsEditor<out com.intellij.execution.configurations.RunConfiguration> {
        return LogosRunConfigurationEditor()
    }

    override fun getState(
        executor: com.intellij.execution.Executor,
        environment: com.intellij.execution.runners.ExecutionEnvironment
    ): com.intellij.execution.configurations.RunProfileState? {
        return LogosRunProfileState(environment)
    }
}

/**
 * Editor for Logos run configuration settings.
 */
class LogosRunConfigurationEditor : com.intellij.openapi.options.SettingsEditor<LogosRunConfiguration>() {

    private val panel = javax.swing.JPanel()

    init {
        panel.add(javax.swing.JLabel("Theos build configuration"))
    }

    override fun resetEditorFrom(configuration: LogosRunConfiguration) {
        // Load settings from configuration
    }

    override fun applyEditorTo(configuration: LogosRunConfiguration) {
        // Save settings to configuration
    }

    override fun createEditor(): javax.swing.JComponent {
        return panel
    }
}

/**
 * Run profile state for executing Theos builds.
 */
class LogosRunProfileState(
    private val environment: com.intellij.execution.runners.ExecutionEnvironment
) : com.intellij.execution.configurations.RunProfileState {

    override fun execute(
        executor: com.intellij.execution.Executor?,
        runner: com.intellij.execution.runners.ProgramRunner<*>
    ): com.intellij.execution.ExecutionResult? {
        // TODO: Execute 'make' command in project directory
        return null
    }
}
