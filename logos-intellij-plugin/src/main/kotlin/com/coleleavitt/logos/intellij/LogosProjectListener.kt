package com.coleleavitt.logos.intellij

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener

/**
 * Project listener for Logos plugin.
 *
 * Handles project lifecycle events.
 */
class LogosProjectListener : ProjectManagerListener {

    @Deprecated("Deprecated in Java")
    override fun projectOpened(project: Project) {
        // Called when a project is opened
        // Could check for Theos Makefile and offer to configure
    }

    @Deprecated("Deprecated in Java")
    override fun projectClosed(project: Project) {
        // Called when a project is closed
    }
}
