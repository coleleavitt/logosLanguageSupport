package com.coleleavitt.logos

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.*
import java.util.concurrent.CompletableFuture

/**
 * Main language server implementation for Logos
 *
 * Supports .x, .xm, .xi, and .xmi files used in Theos tweak development
 */
class LogosLanguageServer : LanguageServer, LanguageClientAware {

    private var client: LanguageClient? = null
    private val textDocumentService: LogosTextDocumentService by lazy {
        LogosTextDocumentService(client)
    }
    private val workspaceService: LogosWorkspaceService by lazy {
        LogosWorkspaceService()
    }

    override fun initialize(params: InitializeParams): CompletableFuture<InitializeResult> {
        return CompletableFuture.supplyAsync {
            val capabilities = ServerCapabilities()

            // Text document sync
            capabilities.textDocumentSync = Either.forLeft(TextDocumentSyncKind.Full)

            // Completion support
            capabilities.completionProvider = CompletionOptions().apply {
                resolveProvider = false
                triggerCharacters = listOf("%", "@", ".", ":")
            }

            // Hover support
            capabilities.hoverProvider = Either.forLeft(true)

            // Definition support
            capabilities.definitionProvider = Either.forLeft(true)

            // References support
            capabilities.referencesProvider = Either.forLeft(true)

            // Document highlight support
            capabilities.documentHighlightProvider = Either.forLeft(true)

            // Document symbol support
            capabilities.documentSymbolProvider = Either.forLeft(true)

            // Workspace symbol support
            capabilities.workspaceSymbolProvider = Either.forLeft(true)

            // Semantic tokens support (for syntax highlighting)
            capabilities.semanticTokensProvider = SemanticTokensWithRegistrationOptions().apply {
                legend = SemanticTokensLegend(
                    listOf(
                        "keyword", "class", "method", "property", "variable",
                        "function", "parameter", "macro", "comment", "string", "number"
                    ),
                    listOf("declaration", "definition", "readonly", "deprecated")
                )
                full = Either.forLeft(true)
            }

            val serverInfo = ServerInfo("Logos Language Server", "1.0.0")
            InitializeResult(capabilities, serverInfo)
        }
    }

    override fun shutdown(): CompletableFuture<Any> {
        return CompletableFuture.completedFuture(null)
    }

    override fun exit() {
        System.exit(0)
    }

    override fun getTextDocumentService(): TextDocumentService {
        return textDocumentService
    }

    override fun getWorkspaceService(): WorkspaceService {
        return workspaceService
    }

    override fun connect(client: LanguageClient) {
        this.client = client
    }
}
