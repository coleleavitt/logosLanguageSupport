package com.coleleavitt.logos

/**
 * Symbol table for tracking declarations in Logos files
 */
class LogosSymbolTable {
    private val hooks = mutableMapOf<String, HookDeclaration>()
    private val subclasses = mutableMapOf<String, SubclassDeclaration>()
    private val groups = mutableMapOf<String, GroupDeclaration>()
    private val functionHooks = mutableMapOf<String, FunctionHookDeclaration>()

    // Track what's been initialized
    private val initializedGroups = mutableSetOf<String>()

    fun addHook(hook: HookDeclaration) {
        hooks[hook.className] = hook
    }

    fun addSubclass(subclass: SubclassDeclaration) {
        subclasses[subclass.className] = subclass
    }

    fun addGroup(group: GroupDeclaration) {
        groups[group.groupName] = group
    }

    fun addFunctionHook(functionHook: FunctionHookDeclaration) {
        functionHooks[functionHook.functionName] = functionHook
    }

    fun markGroupInitialized(groupName: String) {
        initializedGroups.add(groupName)
    }

    fun isGroupInitialized(groupName: String): Boolean {
        return groupName in initializedGroups
    }

    fun getHook(className: String): HookDeclaration? {
        return hooks[className]
    }

    fun getSubclass(className: String): SubclassDeclaration? {
        return subclasses[className]
    }

    fun getGroup(groupName: String): GroupDeclaration? {
        return groups[groupName]
    }

    fun getFunctionHook(functionName: String): FunctionHookDeclaration? {
        return functionHooks[functionName]
    }

    fun getAllHooks(): List<HookDeclaration> = hooks.values.toList()

    fun getAllSubclasses(): List<SubclassDeclaration> = subclasses.values.toList()

    fun getAllGroups(): List<GroupDeclaration> = groups.values.toList()

    fun getAllFunctionHooks(): List<FunctionHookDeclaration> = functionHooks.values.toList()

    fun getAllClasses(): List<String> {
        return (hooks.keys + subclasses.keys).toList()
    }

    fun findMethod(className: String, selector: String): MethodDeclaration? {
        val hook = getHook(className)
        if (hook != null) {
            return hook.methods.find { it.selector == selector }
        }

        val subclass = getSubclass(className)
        if (subclass != null) {
            return subclass.methods.find { it.selector == selector }
        }

        return null
    }

    fun clear() {
        hooks.clear()
        subclasses.clear()
        groups.clear()
        functionHooks.clear()
        initializedGroups.clear()
    }
}

/**
 * Document state tracking for a single file
 */
data class DocumentState(
    val uri: String,
    val version: Int,
    val content: String,
    val tokens: List<LogosToken> = emptyList(),
    val ast: LogosFile? = null,
    val symbolTable: LogosSymbolTable = LogosSymbolTable(),
    val diagnostics: List<LogosDiagnostic> = emptyList()
)

/**
 * Diagnostic message
 */
data class LogosDiagnostic(
    val range: SourceRange,
    val message: String,
    val severity: DiagnosticSeverity
)

enum class DiagnosticSeverity {
    ERROR,
    WARNING,
    INFO,
    HINT
}
