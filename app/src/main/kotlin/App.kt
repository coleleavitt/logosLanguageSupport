package com.coleleavitt.app

import com.coleleavitt.logos.LogosLanguageServer
import org.eclipse.lsp4j.launch.LSPLauncher
import org.eclipse.lsp4j.services.LanguageClient
import java.util.concurrent.Executors
import java.util.logging.LogManager
import java.util.logging.Logger

/**
 * Main entry point for Logos Language Server
 *
 * This language server provides IDE features for Logos (.x, .xm, .xi, .xmi) files
 * used in Theos tweak development for iOS/macOS.
 *
 * Features:
 * - Syntax highlighting via semantic tokens
 * - Code completion for Logos directives and Objective-C classes
 * - Hover information for directives and symbols
 * - Go-to-definition for classes and methods
 * - Diagnostics for syntax errors and warnings
 *
 * Usage:
 *   The server communicates over stdio using the Language Server Protocol.
 *   Launch it from your IDE or editor's LSP client.
 */
fun main() {
    // Configure logging
    LogManager.getLogManager().reset()
    val logger = Logger.getLogger("LogosLanguageServer")

    logger.info("Starting Logos Language Server...")

    try {
        // Create the language server instance
        val server = LogosLanguageServer()

        // Create the launcher with stdio
        val launcher = LSPLauncher.createServerLauncher(
            server,
            System.`in`,
            System.out,
            Executors.newCachedThreadPool()
        ) { it }

        // Connect the client to the server
        val client = launcher.remoteProxy
        server.connect(client)

        logger.info("Logos Language Server started successfully")
        logger.info("Listening on stdio...")

        // Start listening
        val listening = launcher.startListening()

        // Wait for the server to shut down
        listening.get()

        logger.info("Logos Language Server shutting down")
    } catch (e: Exception) {
        logger.severe("Fatal error in Logos Language Server: ${e.message}")
        e.printStackTrace()
        System.exit(1)
    }
}
