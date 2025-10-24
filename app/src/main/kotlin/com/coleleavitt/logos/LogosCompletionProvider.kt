package com.coleleavitt.logos

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either

/**
 * Provides completion items for Logos language
 */
class LogosCompletionProvider(private val symbolTable: LogosSymbolTable) {

    fun provideCompletions(position: Position, documentState: DocumentState): List<CompletionItem> {
        val completions = mutableListOf<CompletionItem>()

        // Add directive completions
        completions.addAll(getDirectiveCompletions())

        // Add class completions
        completions.addAll(getClassCompletions())

        // Add Objective-C runtime completions
        completions.addAll(getObjCRuntimeCompletions())

        return completions
    }

    private fun getDirectiveCompletions(): List<CompletionItem> {
        return listOf(
            createDirectiveCompletion(
                "hook",
                "%hook ClassName\n\t\$0\n%end",
                "Hook into an existing Objective-C class to swizzle methods"
            ),
            createDirectiveCompletion(
                "subclass",
                "%subclass ClassName : SuperclassName\n\t\$0\n%end",
                "Create a new Objective-C class at runtime with custom methods and properties"
            ),
            createDirectiveCompletion(
                "group",
                "%group GroupName\n\t\$0\n%end",
                "Organize hooks into groups for conditional initialization"
            ),
            createDirectiveCompletion(
                "new",
                "%new\n- (void)\${1:methodName} {\n\t\$0\n}",
                "Add a new method to a hooked class"
            ),
            createDirectiveCompletion(
                "orig",
                "%orig",
                "Call the original method implementation"
            ),
            createDirectiveCompletion(
                "orig(...)",
                "%orig(\$0)",
                "Call the original method implementation with custom arguments"
            ),
            createDirectiveCompletion(
                "log",
                "%log;",
                "Log method arguments to syslog"
            ),
            createDirectiveCompletion(
                "init",
                "%init;",
                "Initialize all hooks in the default group"
            ),
            createDirectiveCompletion(
                "init(...)",
                "%init(\${1:GroupName});",
                "Initialize hooks in a specific group"
            ),
            createDirectiveCompletion(
                "ctor",
                "%ctor {\n\t\$0\n}",
                "Constructor block executed after binary load"
            ),
            createDirectiveCompletion(
                "dtor",
                "%dtor {\n\t\$0\n}",
                "Destructor block executed before unload"
            ),
            createDirectiveCompletion(
                "property",
                "%property(nonatomic, retain) \${1:NSString} *\${2:propertyName};",
                "Add an associated object property to a hooked class"
            ),
            createDirectiveCompletion(
                "hookf",
                "%hookf(\${1:returnType}, \${2:functionName}, \${3:args...}) {\n\t\$0\n}",
                "Hook a C function"
            ),
            createDirectiveCompletion(
                "c",
                "%c(\${1:ClassName})",
                "Runtime class lookup (objc_getClass)"
            ),
            createDirectiveCompletion(
                "config",
                "%config(generator=\${1:MobileSubstrate});",
                "Configure Logos preprocessor settings"
            ),
            createDirectiveCompletion(
                "end",
                "%end",
                "Close a %hook, %subclass, or %group block"
            )
        )
    }

    private fun getClassCompletions(): List<CompletionItem> {
        return symbolTable.getAllClasses().map { className ->
            CompletionItem(className).apply {
                kind = CompletionItemKind.Class
                detail = "Logos class"
            }
        }
    }

    private fun getObjCRuntimeCompletions(): List<CompletionItem> {
        val commonClasses = listOf(
            "NSObject", "NSString", "NSArray", "NSDictionary", "NSNumber",
            "UIView", "UIViewController", "UILabel", "UIButton", "UITableView",
            "UIApplication", "UIWindow", "UINavigationController",
            "NSBundle", "NSUserDefaults", "NSNotificationCenter"
        )

        return commonClasses.map { className ->
            CompletionItem(className).apply {
                kind = CompletionItemKind.Class
                detail = "Objective-C class"
            }
        }
    }

    private fun createDirectiveCompletion(
        label: String,
        insertText: String,
        documentation: String
    ): CompletionItem {
        return CompletionItem(label).apply {
            kind = CompletionItemKind.Keyword
            this.insertText = insertText
            insertTextFormat = InsertTextFormat.Snippet
            this.documentation = Either.forLeft(documentation)
            detail = "Logos directive"
        }
    }

    fun providePropertyAttributeCompletions(): List<CompletionItem> {
        return listOf(
            "assign", "retain", "copy", "strong", "weak",
            "nonatomic", "atomic", "readonly", "readwrite",
            "getter=", "setter="
        ).map { attr ->
            CompletionItem(attr).apply {
                kind = CompletionItemKind.Property
                detail = "Property attribute"
            }
        }
    }

    fun provideConfigCompletions(): List<CompletionItem> {
        return listOf(
            CompletionItem("generator=MobileSubstrate").apply {
                kind = CompletionItemKind.Value
                detail = "Use MobileSubstrate hooking backend"
            },
            CompletionItem("generator=internal").apply {
                kind = CompletionItemKind.Value
                detail = "Use Objective-C runtime directly"
            },
            CompletionItem("generator=libhooker").apply {
                kind = CompletionItemKind.Value
                detail = "Use libhooker hooking backend"
            },
            CompletionItem("warnings=default").apply {
                kind = CompletionItemKind.Value
                detail = "Default warning behavior"
            },
            CompletionItem("warnings=error").apply {
                kind = CompletionItemKind.Value
                detail = "Treat warnings as errors"
            },
            CompletionItem("warnings=none").apply {
                kind = CompletionItemKind.Value
                detail = "Suppress warnings"
            }
        )
    }
}
