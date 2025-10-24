package com.coleleavitt.logos

/**
 * Provides diagnostics (errors, warnings) for Logos files
 */
class LogosDiagnosticsProvider {

    fun provideDiagnostics(documentState: DocumentState): List<LogosDiagnostic> {
        val diagnostics = mutableListOf<LogosDiagnostic>()

        // Check for unmatched %hook/%end pairs
        diagnostics.addAll(checkUnmatchedBlocks(documentState.tokens))

        // Check for %orig in %new methods
        diagnostics.addAll(checkOrigInNewMethods(documentState))

        // Check for uninitialized groups
        diagnostics.addAll(checkUninitializedGroups(documentState))

        // Check for property attributes
        diagnostics.addAll(checkPropertyAttributes(documentState))

        return diagnostics
    }

    private fun checkUnmatchedBlocks(tokens: List<LogosToken>): List<LogosDiagnostic> {
        val diagnostics = mutableListOf<LogosDiagnostic>()
        val blockStack = mutableListOf<LogosToken>()

        for (token in tokens) {
            when (token.type) {
                LogosTokenType.DIRECTIVE_HOOK,
                LogosTokenType.DIRECTIVE_SUBCLASS,
                LogosTokenType.DIRECTIVE_GROUP -> {
                    blockStack.add(token)
                }
                LogosTokenType.DIRECTIVE_END -> {
                    if (blockStack.isEmpty()) {
                        diagnostics.add(
                            LogosDiagnostic(
                                range = tokenToRange(token),
                                message = "Unexpected %end without matching %hook, %subclass, or %group",
                                severity = DiagnosticSeverity.ERROR
                            )
                        )
                    } else {
                        blockStack.removeAt(blockStack.size - 1)
                    }
                }
                else -> {}
            }
        }

        // Check for unclosed blocks
        for (unclosedBlock in blockStack) {
            diagnostics.add(
                LogosDiagnostic(
                    range = tokenToRange(unclosedBlock),
                    message = "Missing %end for ${unclosedBlock.text}",
                    severity = DiagnosticSeverity.ERROR
                )
            )
        }

        return diagnostics
    }

    private fun checkOrigInNewMethods(documentState: DocumentState): List<LogosDiagnostic> {
        val diagnostics = mutableListOf<LogosDiagnostic>()

        documentState.ast?.let { file ->
            // Visit all method declarations
            for (decl in file.declarations) {
                when (decl) {
                    is HookDeclaration -> {
                        for (method in decl.methods) {
                            if (method.isNew && method.body?.contains("%orig") == true) {
                                method.range?.let { range ->
                                    diagnostics.add(
                                        LogosDiagnostic(
                                            range = range,
                                            message = "%orig does not work in %new methods",
                                            severity = DiagnosticSeverity.WARNING
                                        )
                                    )
                                }
                            }
                        }
                    }
                    is SubclassDeclaration -> {
                        for (method in decl.methods) {
                            if (method.isNew && method.body?.contains("%orig") == true) {
                                method.range?.let { range ->
                                    diagnostics.add(
                                        LogosDiagnostic(
                                            range = range,
                                            message = "%orig does not work in %new methods",
                                            severity = DiagnosticSeverity.WARNING
                                        )
                                    )
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
        }

        return diagnostics
    }

    private fun checkUninitializedGroups(documentState: DocumentState): List<LogosDiagnostic> {
        val diagnostics = mutableListOf<LogosDiagnostic>()

        // Find all groups
        val groups = documentState.symbolTable.getAllGroups()

        // Check which groups are initialized
        for (group in groups) {
            if (!documentState.symbolTable.isGroupInitialized(group.groupName)) {
                group.range?.let { range ->
                    diagnostics.add(
                        LogosDiagnostic(
                            range = range,
                            message = "Group '${group.groupName}' is never initialized. Add %init(${group.groupName}); in a constructor.",
                            severity = DiagnosticSeverity.WARNING
                        )
                    )
                }
            }
        }

        return diagnostics
    }

    private fun checkPropertyAttributes(documentState: DocumentState): List<LogosDiagnostic> {
        val diagnostics = mutableListOf<LogosDiagnostic>()

        documentState.ast?.let { file ->
            for (decl in file.declarations) {
                val properties = when (decl) {
                    is HookDeclaration -> decl.properties
                    is SubclassDeclaration -> decl.properties
                    else -> emptyList()
                }

                for (property in properties) {
                    val attrs = property.attributes

                    // Check for conflicting memory management attributes
                    val hasAssign = attrs.contains("assign") || attrs.contains("unsafe_unretained")
                    val hasRetain = attrs.contains("retain") || attrs.contains("strong")
                    val hasCopy = attrs.contains("copy")

                    val memoryAttrs = listOf(hasAssign, hasRetain, hasCopy).count { it }
                    if (memoryAttrs > 1) {
                        property.range?.let { range ->
                            diagnostics.add(
                                LogosDiagnostic(
                                    range = range,
                                    message = "Property has conflicting memory management attributes",
                                    severity = DiagnosticSeverity.ERROR
                                )
                            )
                        }
                    }

                    // Check for readonly with memory management
                    if (attrs.contains("readonly") && memoryAttrs > 0) {
                        property.range?.let { range ->
                            diagnostics.add(
                                LogosDiagnostic(
                                    range = range,
                                    message = "Property attribute 'readonly' cannot be used with memory management attributes",
                                    severity = DiagnosticSeverity.ERROR
                                )
                            )
                        }
                    }
                }
            }
        }

        return diagnostics
    }

    private fun tokenToRange(token: LogosToken): SourceRange {
        return SourceRange(
            start = SourcePosition(token.line, token.column, 0),
            end = SourcePosition(token.line, token.endColumn, 0)
        )
    }
}
