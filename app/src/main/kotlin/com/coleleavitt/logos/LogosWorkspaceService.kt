package com.coleleavitt.logos

import org.eclipse.lsp4j.DidChangeConfigurationParams
import org.eclipse.lsp4j.DidChangeWatchedFilesParams
import org.eclipse.lsp4j.services.WorkspaceService

/**
 * Workspace service for Logos language server
 */
class LogosWorkspaceService : WorkspaceService {

    override fun didChangeConfiguration(params: DidChangeConfigurationParams) {
        // Handle configuration changes
    }

    override fun didChangeWatchedFiles(params: DidChangeWatchedFilesParams) {
        // Handle file system changes
    }
}
