package com.coleleavitt.logos

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.TextDocumentService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * Text document service for Logos language server
 */
class LogosTextDocumentService(private val client: org.eclipse.lsp4j.services.LanguageClient?) : TextDocumentService {

    private val documents = ConcurrentHashMap<String, DocumentState>()

    override fun didOpen(params: DidOpenTextDocumentParams) {
        val textDocument = params.textDocument
        val uri = textDocument.uri
        val content = textDocument.text

        // Tokenize and analyze
        val lexer = LogosLexer(content)
        val tokens = lexer.tokenize()

        val symbolTable = LogosSymbolTable()
        // TODO: Parse and build AST

        val diagnosticsProvider = LogosDiagnosticsProvider()
        val documentState = DocumentState(
            uri = uri,
            version = textDocument.version,
            content = content,
            tokens = tokens,
            symbolTable = symbolTable
        )

        val diagnostics = diagnosticsProvider.provideDiagnostics(documentState)
        documents[uri] = documentState.copy(diagnostics = diagnostics)

        // Publish diagnostics
        publishDiagnostics(uri, diagnostics)
    }

    override fun didChange(params: DidChangeTextDocumentParams) {
        val uri = params.textDocument.uri
        val changes = params.contentChanges

        val currentDoc = documents[uri] ?: return

        // For simplicity, we handle full document sync
        val newContent = changes.firstOrNull()?.text ?: return

        // Re-tokenize and analyze
        val lexer = LogosLexer(newContent)
        val tokens = lexer.tokenize()

        val symbolTable = LogosSymbolTable()
        // TODO: Parse and build AST

        val diagnosticsProvider = LogosDiagnosticsProvider()
        val documentState = DocumentState(
            uri = uri,
            version = params.textDocument.version,
            content = newContent,
            tokens = tokens,
            symbolTable = symbolTable
        )

        val diagnostics = diagnosticsProvider.provideDiagnostics(documentState)
        documents[uri] = documentState.copy(diagnostics = diagnostics)

        // Publish diagnostics
        publishDiagnostics(uri, diagnostics)
    }

    override fun didClose(params: DidCloseTextDocumentParams) {
        val uri = params.textDocument.uri
        documents.remove(uri)
    }

    override fun didSave(params: DidSaveTextDocumentParams) {
        // Nothing special to do on save
    }

    override fun completion(params: CompletionParams): CompletableFuture<Either<MutableList<CompletionItem>, CompletionList>> {
        return CompletableFuture.supplyAsync {
            val uri = params.textDocument.uri
            val documentState = documents[uri] ?: return@supplyAsync Either.forLeft(mutableListOf())

            val completionProvider = LogosCompletionProvider(documentState.symbolTable)
            val completions = completionProvider.provideCompletions(params.position, documentState)

            Either.forLeft(completions.toMutableList())
        }
    }

    override fun hover(params: HoverParams): CompletableFuture<Hover?> {
        return CompletableFuture.supplyAsync {
            val uri = params.textDocument.uri
            val documentState = documents[uri] ?: return@supplyAsync null

            val hoverProvider = LogosHoverProvider(documentState.symbolTable)
            hoverProvider.provideHover(params.position, documentState)
        }
    }

    override fun definition(params: DefinitionParams): CompletableFuture<Either<MutableList<out Location>, MutableList<out LocationLink>>> {
        return CompletableFuture.supplyAsync {
            val uri = params.textDocument.uri
            val documentState = documents[uri] ?: return@supplyAsync Either.forLeft(mutableListOf<Location>())

            // TODO: Implement go-to-definition
            // For now, return empty
            Either.forLeft(mutableListOf<Location>())
        }
    }

    override fun references(params: ReferenceParams): CompletableFuture<MutableList<out Location>> {
        return CompletableFuture.supplyAsync {
            // TODO: Implement find references
            mutableListOf<Location>()
        }
    }

    override fun documentHighlight(params: DocumentHighlightParams): CompletableFuture<MutableList<out DocumentHighlight>> {
        return CompletableFuture.supplyAsync {
            // TODO: Implement document highlighting
            mutableListOf<DocumentHighlight>()
        }
    }

    override fun documentSymbol(params: DocumentSymbolParams): CompletableFuture<MutableList<Either<SymbolInformation, DocumentSymbol>>> {
        return CompletableFuture.supplyAsync {
            val uri = params.textDocument.uri
            val documentState = documents[uri] ?: return@supplyAsync mutableListOf()

            val symbols = mutableListOf<Either<SymbolInformation, DocumentSymbol>>()

            // Add hooks as symbols
            for (hook in documentState.symbolTable.getAllHooks()) {
                val symbol = DocumentSymbol(
                    hook.className,
                    SymbolKind.Class,
                    rangeToLspRange(hook.range),
                    rangeToLspRange(hook.range),
                    ""
                )
                symbols.add(Either.forRight(symbol))
            }

            // Add subclasses as symbols
            for (subclass in documentState.symbolTable.getAllSubclasses()) {
                val symbol = DocumentSymbol(
                    subclass.className,
                    SymbolKind.Class,
                    rangeToLspRange(subclass.range),
                    rangeToLspRange(subclass.range),
                    ""
                )
                symbols.add(Either.forRight(symbol))
            }

            symbols
        }
    }

    private fun publishDiagnostics(uri: String, diagnostics: List<LogosDiagnostic>) {
        val lspDiagnostics = diagnostics.map { diag ->
            Diagnostic(
                rangeToLspRange(diag.range),
                diag.message,
                severityToLspSeverity(diag.severity),
                "logos"
            )
        }

        client?.publishDiagnostics(PublishDiagnosticsParams(uri, lspDiagnostics))
    }

    private fun rangeToLspRange(range: SourceRange?): Range {
        return range?.let {
            Range(
                Position(it.start.line, it.start.column),
                Position(it.end.line, it.end.column)
            )
        } ?: Range(Position(0, 0), Position(0, 0))
    }

    private fun severityToLspSeverity(severity: DiagnosticSeverity): org.eclipse.lsp4j.DiagnosticSeverity {
        return when (severity) {
            DiagnosticSeverity.ERROR -> org.eclipse.lsp4j.DiagnosticSeverity.Error
            DiagnosticSeverity.WARNING -> org.eclipse.lsp4j.DiagnosticSeverity.Warning
            DiagnosticSeverity.INFO -> org.eclipse.lsp4j.DiagnosticSeverity.Information
            DiagnosticSeverity.HINT -> org.eclipse.lsp4j.DiagnosticSeverity.Hint
        }
    }
}
