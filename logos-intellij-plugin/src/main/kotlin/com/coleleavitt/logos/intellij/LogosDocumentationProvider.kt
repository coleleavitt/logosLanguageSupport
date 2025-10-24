package com.coleleavitt.logos.intellij

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement

/**
 * Documentation provider for Logos language.
 *
 * Provides hover documentation for Logos directives and symbols.
 * This integrates with the LSP hover provider for comprehensive documentation.
 */
class LogosDocumentationProvider : AbstractDocumentationProvider() {

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element !is LogosPsiElement) {
            return null
        }

        val node = element.node
        val elementType = node.elementType

        return when (elementType) {
            LogosElementTypes.DIRECTIVE_HOOK -> generateHookDoc()
            LogosElementTypes.DIRECTIVE_SUBCLASS -> generateSubclassDoc()
            LogosElementTypes.DIRECTIVE_NEW -> generateNewDoc()
            LogosElementTypes.DIRECTIVE_ORIG -> generateOrigDoc()
            LogosElementTypes.DIRECTIVE_ORIG_PTR -> generateOrigPtrDoc()
            LogosElementTypes.DIRECTIVE_LOG -> generateLogDoc()
            LogosElementTypes.DIRECTIVE_INIT -> generateInitDoc()
            LogosElementTypes.DIRECTIVE_CTOR -> generateCtorDoc()
            LogosElementTypes.DIRECTIVE_DTOR -> generateDtorDoc()
            LogosElementTypes.DIRECTIVE_GROUP -> generateGroupDoc()
            LogosElementTypes.DIRECTIVE_PROPERTY -> generatePropertyDoc()
            LogosElementTypes.DIRECTIVE_HOOKF -> generateHookfDoc()
            LogosElementTypes.DIRECTIVE_C -> generateCDoc()
            LogosElementTypes.DIRECTIVE_CONFIG -> generateConfigDoc()
            else -> null
        }
    }

    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        return generateDoc(element, originalElement)
    }

    private fun generateHookDoc() = """
        <h2>%hook</h2>
        <p>Hook an existing Objective-C class to override its methods.</p>
        <h3>Syntax:</h3>
        <pre>%hook ClassName
- (returnType)methodName:(argType)arg {
    // Implementation
}
%end</pre>
        <h3>Example:</h3>
        <pre>%hook SpringBoard
- (void)applicationDidFinishLaunching:(id)app {
    %orig; // Call original
    NSLog(@"Hooked!");
}
%end</pre>
    """.trimIndent()

    private fun generateSubclassDoc() = """
        <h2>%subclass</h2>
        <p>Create a new Objective-C class at runtime that inherits from an existing class.</p>
        <h3>Syntax:</h3>
        <pre>%subclass NewClass : ParentClass
- (void)customMethod {
    // Implementation
}
%end</pre>
    """.trimIndent()

    private fun generateNewDoc() = """
        <h2>%new</h2>
        <p>Add a completely new method to a hooked class.</p>
        <h3>Syntax:</h3>
        <pre>%new
- (void)customMethod {
    // Implementation
}</pre>
    """.trimIndent()

    private fun generateOrigDoc() = """
        <h2>%orig</h2>
        <p>Call the original implementation of a hooked method.</p>
        <h3>Usage:</h3>
        <pre>- (void)method {
    %orig; // Calls original
    // Additional code
}</pre>
        <h3>With custom arguments:</h3>
        <pre>- (NSString *)description {
    return [%orig stringByAppendingString:@" (modified)"];
}</pre>
    """.trimIndent()

    private fun generateOrigPtrDoc() = """
        <h2>&%orig</h2>
        <p>Get a pointer to the original method implementation (IMP).</p>
        <h3>Usage:</h3>
        <pre>- (void)method {
    IMP originalIMP = &%orig;
    // Store or call later
    ((void(*)(id, SEL))originalIMP)(self, _cmd);
}</pre>
        <h3>Use case:</h3>
        <p>Advanced hooking patterns where you need to store the function pointer for later use.</p>
    """.trimIndent()

    private fun generateLogDoc() = """
        <h2>%log</h2>
        <p>Log all arguments and return value of the current method.</p>
        <h3>Usage:</h3>
        <pre>- (void)method:(id)arg {
    %log; // Logs all parameters
}</pre>
    """.trimIndent()

    private fun generateInitDoc() = """
        <h2>%init</h2>
        <p>Initialize hooks, optionally for a specific group.</p>
        <h3>Syntax:</h3>
        <pre>%init; // Initialize all hooks
%init(GroupName); // Initialize specific group</pre>
    """.trimIndent()

    private fun generateCtorDoc() = """
        <h2>%ctor</h2>
        <p>Constructor block - runs when the tweak is loaded.</p>
        <h3>Syntax:</h3>
        <pre>%ctor {
    %init;
    NSLog(@"Tweak loaded");
}</pre>
    """.trimIndent()

    private fun generateDtorDoc() = """
        <h2>%dtor</h2>
        <p>Destructor block - runs when the tweak is unloaded.</p>
        <h3>Syntax:</h3>
        <pre>%dtor {
    NSLog(@"Tweak unloaded");
}</pre>
    """.trimIndent()

    private fun generateGroupDoc() = """
        <h2>%group</h2>
        <p>Group hooks together for conditional initialization.</p>
        <h3>Syntax:</h3>
        <pre>%group GroupName
%hook ClassName
// Methods
%end
%end</pre>
    """.trimIndent()

    private fun generatePropertyDoc() = """
        <h2>%property</h2>
        <p>Add an associated property to a hooked class.</p>
        <h3>Syntax:</h3>
        <pre>%property (nonatomic, strong) NSString *customProperty;</pre>
    """.trimIndent()

    private fun generateHookfDoc() = """
        <h2>%hookf</h2>
        <p>Hook a C function.</p>
        <h3>Syntax:</h3>
        <pre>%hookf(returnType, functionName, args) {
    // Implementation
}</pre>
    """.trimIndent()

    private fun generateCDoc() = """
        <h2>%c</h2>
        <p>Runtime class lookup - get a class by name at runtime.</p>
        <h3>Syntax:</h3>
        <pre>Class cls = %c(ClassName);</pre>
    """.trimIndent()

    private fun generateConfigDoc() = """
        <h2>%config</h2>
        <p>Configure Logos preprocessor options.</p>
        <h3>Syntax:</h3>
        <pre>%config(generator=internal)</pre>
    """.trimIndent()
}
