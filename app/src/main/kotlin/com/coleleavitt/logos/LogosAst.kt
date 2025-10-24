package com.coleleavitt.logos

/**
 * Abstract Syntax Tree nodes for Logos
 */
sealed class LogosNode {
    abstract val range: SourceRange?
}

/**
 * Root node containing all top-level declarations
 */
data class LogosFile(
    val declarations: List<LogosNode>,
    override val range: SourceRange? = null
) : LogosNode()

/**
 * %hook ClassName ... %end
 */
data class HookDeclaration(
    val className: String,
    val methods: List<MethodDeclaration>,
    val properties: List<PropertyDeclaration>,
    val classExpression: String? = null, // For Swift classes
    override val range: SourceRange? = null
) : LogosNode()

/**
 * %subclass ClassName : SuperclassName <Protocols> ... %end
 */
data class SubclassDeclaration(
    val className: String,
    val superclassName: String,
    val protocols: List<String>,
    val methods: List<MethodDeclaration>,
    val properties: List<PropertyDeclaration>,
    override val range: SourceRange? = null
) : LogosNode()

/**
 * %group GroupName ... %end
 */
data class GroupDeclaration(
    val groupName: String,
    val declarations: List<LogosNode>,
    override val range: SourceRange? = null
) : LogosNode()

/**
 * Method declaration: [+-] (returnType)methodName:...
 */
data class MethodDeclaration(
    val scope: String, // "+" or "-"
    val returnType: String,
    val selectorParts: List<String>,
    val parameters: List<MethodParameter>,
    val isNew: Boolean = false,
    val typeEncoding: String? = null,
    val body: String? = null,
    override val range: SourceRange? = null
) : LogosNode() {
    val selector: String
        get() = if (parameters.isEmpty()) {
            selectorParts.first()
        } else {
            selectorParts.joinToString(":")+ ":"
        }

    val methodSignature: String
        get() = "$scope ($returnType)$selector"
}

/**
 * Method parameter
 */
data class MethodParameter(
    val type: String,
    val name: String
)

/**
 * %property(attributes) type name;
 */
data class PropertyDeclaration(
    val attributes: List<String>,
    val type: String,
    val name: String,
    val getter: String? = null,
    val setter: String? = null,
    override val range: SourceRange? = null
) : LogosNode()

/**
 * %hookf(returnType, functionName, args...)
 */
data class FunctionHookDeclaration(
    val returnType: String,
    val functionName: String,
    val parameters: List<String>,
    val body: String? = null,
    override val range: SourceRange? = null
) : LogosNode()

/**
 * %ctor { ... }
 */
data class ConstructorDeclaration(
    val body: String,
    override val range: SourceRange? = null
) : LogosNode()

/**
 * %dtor { ... }
 */
data class DestructorDeclaration(
    val body: String,
    override val range: SourceRange? = null
) : LogosNode()

/**
 * %init / %init(GroupName, ClassName=expression, ...)
 */
data class InitDirective(
    val groupName: String? = null,
    val classBindings: Map<String, String> = emptyMap(),
    override val range: SourceRange? = null
) : LogosNode()

/**
 * %config(key=value)
 */
data class ConfigDirective(
    val key: String,
    val value: String,
    override val range: SourceRange? = null
) : LogosNode()
