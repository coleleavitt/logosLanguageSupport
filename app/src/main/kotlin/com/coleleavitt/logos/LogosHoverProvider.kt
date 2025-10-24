package com.coleleavitt.logos

import org.eclipse.lsp4j.*

/**
 * Provides hover information for Logos language
 */
class LogosHoverProvider(private val symbolTable: LogosSymbolTable) {

    fun provideHover(position: Position, documentState: DocumentState): Hover? {
        val token = findTokenAtPosition(position, documentState.tokens) ?: return null

        return when (token.type) {
            LogosTokenType.DIRECTIVE_HOOK -> createDirectiveHover(
                "**%hook** *ClassName*",
                """
                Opens a hook block for swizzling methods in an existing Objective-C class.

                **Example:**
                ```objc
                %hook NSObject
                - (NSString *)description {
                    return [%orig stringByAppendingString:@" (modified)"];
                }
                %end
                ```

                **Swift Classes:**
                For bridged Swift classes, use dot notation:
                ```objc
                %hook MyApp.MyClass
                ```
                """.trimIndent()
            )

            LogosTokenType.DIRECTIVE_SUBCLASS -> createDirectiveHover(
                "**%subclass** *ClassName* **:** *SuperclassName* **<***Protocols***>**",
                """
                Creates a new Objective-C class at runtime with custom methods and properties.

                **Example:**
                ```objc
                %subclass MyCustomClass : NSObject <NSCopying>
                %property(nonatomic, retain) NSString *name;

                - (instancetype)init {
                    if (self = %orig) {
                        self.name = @"Custom";
                    }
                    return self;
                }
                %end
                ```
                """.trimIndent()
            )

            LogosTokenType.DIRECTIVE_GROUP -> createDirectiveHover(
                "**%group** *GroupName*",
                """
                Organizes hooks into named groups for conditional initialization.
                Useful for iOS version-specific hooks.

                **Example:**
                ```objc
                %group iOS14
                %hook UIView
                // iOS 14 specific hooks
                %end
                %end

                %ctor {
                    if (@available(iOS 14, *)) {
                        %init(iOS14);
                    }
                }
                ```
                """.trimIndent()
            )

            LogosTokenType.DIRECTIVE_NEW -> createDirectiveHover(
                "**%new**[**(***type-encoding***)**]",
                """
                Declares a new method added to a hooked class.

                **Example:**
                ```objc
                %hook NSObject
                %new
                - (void)customMethod {
                    NSLog(@"New method!");
                }
                %end
                ```

                **With type encoding:**
                ```objc
                %new(v@:)
                - (void)anotherMethod {
                    // ...
                }
                ```
                """.trimIndent()
            )

            LogosTokenType.DIRECTIVE_ORIG -> createDirectiveHover(
                "**%orig** / **%orig**(*args...*)",
                """
                Calls the original method implementation.

                **Without arguments** (uses original arguments):
                ```objc
                - (void)someMethod {
                    %orig;  // Calls original with same arguments
                }
                ```

                **With custom arguments:**
                ```objc
                - (void)setValue:(id)value {
                    %orig(modifiedValue);  // Call with different value
                }
                ```

                **Get function pointer:**
                ```objc
                void (*originalFunc)() = &%orig;
                ```

                **Note:** %orig does not work in %new methods.
                """.trimIndent()
            )

            LogosTokenType.DIRECTIVE_LOG -> createDirectiveHover(
                "**%log** / **%log**(*args...*)",
                """
                Dumps method arguments to syslog for debugging.

                **Example:**
                ```objc
                - (void)someMethod:(NSString *)arg {
                    %log;  // Logs: self, _cmd, arg
                }
                ```
                """.trimIndent()
            )

            LogosTokenType.DIRECTIVE_INIT -> createDirectiveHover(
                "**%init** / **%init**(*GroupName*, *bindings...*)",
                """
                Initializes hooks, typically called in %ctor.

                **Default group:**
                ```objc
                %init;
                ```

                **Named group:**
                ```objc
                %init(iOS14Hooks);
                ```

                **With class bindings:**
                ```objc
                %init(ClassName=objc_getClass("Custom.Class"));
                ```

                **Dynamic function binding:**
                ```objc
                %init(MyFunction=MSFindSymbol(NULL, "_MyFunction"));
                ```
                """.trimIndent()
            )

            LogosTokenType.DIRECTIVE_CTOR -> createDirectiveHover(
                "**%ctor** { ... }",
                """
                Constructor block executed when the binary is loaded.
                Equivalent to `__attribute__((constructor))`.

                **Example:**
                ```objc
                %ctor {
                    %init;  // Initialize all hooks
                    NSLog(@"Tweak loaded");
                }
                ```
                """.trimIndent()
            )

            LogosTokenType.DIRECTIVE_DTOR -> createDirectiveHover(
                "**%dtor** { ... }",
                """
                Destructor block executed before the binary unloads.
                Equivalent to `__attribute__((destructor))`.

                **Example:**
                ```objc
                %dtor {
                    NSLog(@"Tweak unloading");
                }
                ```
                """.trimIndent()
            )

            LogosTokenType.DIRECTIVE_PROPERTY -> createDirectiveHover(
                "**%property**(*attributes*) *type* *name***;**",
                """
                Adds an associated object property to a hooked class.

                **Attributes:**
                - Memory: `assign`, `retain`, `copy`, `strong`, `weak`
                - Threading: `atomic`, `nonatomic`
                - Access: `readonly`, `readwrite`
                - Custom: `getter=name`, `setter=name:`

                **Example:**
                ```objc
                %hook UIView
                %property(nonatomic, retain) NSString *customIdentifier;

                - (void)someMethod {
                    self.customIdentifier = @"test";
                    NSLog(@"%@", self.customIdentifier);
                }
                %end
                ```
                """.trimIndent()
            )

            LogosTokenType.DIRECTIVE_HOOKF -> createDirectiveHover(
                "**%hookf**(*returnType*, *functionName*, *args...*)",
                """
                Hooks a C function.

                **Example:**
                ```objc
                %hookf(BOOL, MGGetBoolAnswer, CFStringRef key) {
                    if (CFEqual(key, CFSTR("SomeKey"))) {
                        return YES;
                    }
                    return %orig;
                }

                %ctor {
                    %init(MGGetBoolAnswer = MSFindSymbol(NULL, "_MGGetBoolAnswer"));
                }
                ```
                """.trimIndent()
            )

            LogosTokenType.DIRECTIVE_C -> createDirectiveHover(
                "**%c**([**+**|**-**]*ClassName*)",
                """
                Runtime class lookup using objc_getClass().

                **Instance class:**
                ```objc
                Class myClass = %c(MyClass);
                ```

                **Meta class:**
                ```objc
                Class myMetaClass = %c(+MyClass);
                ```

                **Swift classes:**
                ```objc
                Class swiftClass = %c(MyApp.SwiftClass);
                ```
                """.trimIndent()
            )

            LogosTokenType.DIRECTIVE_CONFIG -> createDirectiveHover(
                "**%config**(*key***=***value*)",
                """
                Configures Logos preprocessor settings.

                **Generator options:**
                - `generator=MobileSubstrate` - Use MobileSubstrate (default)
                - `generator=internal` - Use Objective-C runtime directly
                - `generator=libhooker` - Use libhooker

                **Warning options:**
                - `warnings=default` - Normal warnings
                - `warnings=error` - Treat warnings as errors
                - `warnings=none` - Suppress warnings

                **Example:**
                ```objc
                %config(generator=internal);
                ```
                """.trimIndent()
            )

            LogosTokenType.DIRECTIVE_END -> createDirectiveHover(
                "**%end**",
                "Closes a %hook, %subclass, or %group block."
            )

            else -> {
                // Check if it's a class name
                val className = token.text
                val hook = symbolTable.getHook(className)
                if (hook != null) {
                    return createClassHover(hook)
                }

                val subclass = symbolTable.getSubclass(className)
                if (subclass != null) {
                    return createSubclassHover(subclass)
                }

                null
            }
        }
    }

    private fun findTokenAtPosition(position: Position, tokens: List<LogosToken>): LogosToken? {
        return tokens.find { token ->
            token.line == position.line &&
            position.character >= token.column &&
            position.character < token.endColumn
        }
    }

    private fun createDirectiveHover(syntax: String, documentation: String): Hover {
        val contents = MarkupContent().apply {
            kind = "markdown"
            value = "$syntax\n\n---\n\n$documentation"
        }
        return Hover(contents)
    }

    private fun createClassHover(hook: HookDeclaration): Hover {
        val methodsList = hook.methods.joinToString("\n") { "- ${it.methodSignature}" }
        val contents = MarkupContent().apply {
            kind = "markdown"
            value = """
                **Hooked Class:** `${hook.className}`

                **Methods (${hook.methods.size}):**
                $methodsList
            """.trimIndent()
        }
        return Hover(contents)
    }

    private fun createSubclassHover(subclass: SubclassDeclaration): Hover {
        val methodsList = subclass.methods.joinToString("\n") { "- ${it.methodSignature}" }
        val protocolsList = if (subclass.protocols.isNotEmpty()) {
            "\n**Protocols:** ${subclass.protocols.joinToString(", ")}"
        } else ""

        val contents = MarkupContent().apply {
            kind = "markdown"
            value = """
                **Subclass:** `${subclass.className}` : `${subclass.superclassName}`$protocolsList

                **Methods (${subclass.methods.size}):**
                $methodsList
            """.trimIndent()
        }
        return Hover(contents)
    }
}
