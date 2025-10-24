package com.coleleavitt.logos.intellij

import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.wm.IdeFrame

/**
 * Application listener for Logos plugin.
 *
 * Handles application-level events.
 */
class LogosApplicationListener : ApplicationActivationListener {

    override fun applicationActivated(ideFrame: IdeFrame) {
        // Called when the application gains focus
    }

    override fun applicationDeactivated(ideFrame: IdeFrame) {
        // Called when the application loses focus
    }
}
