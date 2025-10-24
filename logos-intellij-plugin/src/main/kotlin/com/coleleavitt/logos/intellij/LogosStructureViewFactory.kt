package com.coleleavitt.logos.intellij

import com.intellij.ide.structureView.StructureViewBuilder
import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiElement

/**
 * Structure view factory for Logos files.
 *
 * Provides outline view showing:
 * - %hook blocks
 * - %subclass blocks
 * - %group blocks
 * - Methods within blocks
 */
class LogosStructureViewFactory : PsiStructureViewFactory {

    override fun getStructureViewBuilder(psiFile: PsiFile): StructureViewBuilder? {
        return object : TreeBasedStructureViewBuilder() {
            override fun createStructureViewModel(editor: Editor?): StructureViewModel {
                return LogosStructureViewModel(psiFile)
            }
        }
    }
}

/**
 * Structure view model for Logos files.
 */
class LogosStructureViewModel(psiFile: PsiFile) :
    com.intellij.ide.structureView.TextEditorBasedStructureViewModel(psiFile) {

    override fun getPsiFile(): PsiFile {
        return super.getPsiFile()
    }

    override fun getRoot(): com.intellij.ide.structureView.StructureViewTreeElement {
        return LogosStructureViewElement(psiFile)
    }
}

/**
 * Structure view element for Logos PSI elements.
 */
class LogosStructureViewElement(private val element: PsiElement) :
    com.intellij.ide.structureView.StructureViewTreeElement {

    override fun getValue(): Any {
        return element
    }

    override fun navigate(requestFocus: Boolean) {
        if (element is com.intellij.pom.Navigatable) {
            element.navigate(requestFocus)
        }
    }

    override fun canNavigate(): Boolean {
        return element is com.intellij.pom.Navigatable && element.canNavigate()
    }

    override fun canNavigateToSource(): Boolean {
        return element is com.intellij.pom.Navigatable && element.canNavigateToSource()
    }

    override fun getPresentation(): com.intellij.navigation.ItemPresentation {
        return object : com.intellij.navigation.ItemPresentation {
            override fun getPresentableText(): String? {
                if (element !is LogosPsiElement) {
                    return element.containingFile?.name ?: "Logos File"
                }

                val elementType = element.node.elementType
                val text = element.text

                return when (elementType) {
                    LogosElementTypes.DIRECTIVE_HOOK -> {
                        // Extract class name after %hook
                        text.substringAfter("%hook").trim().takeWhile { it.isLetterOrDigit() || it == '_' || it == '$' }
                            .let { if (it.isNotEmpty()) "%hook $it" else text.take(50) }
                    }
                    LogosElementTypes.DIRECTIVE_SUBCLASS -> {
                        // Extract class name after %subclass
                        text.substringAfter("%subclass").trim().takeWhile { it.isLetterOrDigit() || it == '_' || it == ':' || it.isWhitespace() }
                            .let { if (it.isNotEmpty()) "%subclass $it" else text.take(50) }
                    }
                    LogosElementTypes.DIRECTIVE_GROUP -> {
                        // Extract group name after %group
                        text.substringAfter("%group").trim().takeWhile { it.isLetterOrDigit() || it == '_' }
                            .let { if (it.isNotEmpty()) "%group $it" else text.take(50) }
                    }
                    LogosElementTypes.OBJ_C_METHOD_SCOPE -> {
                        // This is a method signature, get the full line
                        text.take(100).replace("\n", " ").trim()
                    }
                    else -> text.take(50)
                }
            }

            override fun getLocationString(): String? {
                return null
            }

            override fun getIcon(unused: Boolean): javax.swing.Icon? {
                if (element !is LogosPsiElement) {
                    return LogosIcons.FILE
                }

                return when (element.node.elementType) {
                    LogosElementTypes.DIRECTIVE_HOOK -> com.intellij.icons.AllIcons.Nodes.Class
                    LogosElementTypes.DIRECTIVE_SUBCLASS -> com.intellij.icons.AllIcons.Nodes.AbstractClass
                    LogosElementTypes.DIRECTIVE_GROUP -> com.intellij.icons.AllIcons.Nodes.Package
                    LogosElementTypes.OBJ_C_METHOD_SCOPE -> com.intellij.icons.AllIcons.Nodes.Method
                    else -> LogosIcons.FILE
                }
            }
        }
    }

    override fun getChildren(): Array<com.intellij.ide.util.treeView.smartTree.TreeElement> {
        val children = mutableListOf<com.intellij.ide.util.treeView.smartTree.TreeElement>()

        // Collect all child elements
        var child: PsiElement? = element.firstChild
        while (child != null) {
            if (child is LogosPsiElement) {
                val childType = child.node.elementType

                // Add structural elements to the tree
                when (childType) {
                    LogosElementTypes.DIRECTIVE_HOOK,
                    LogosElementTypes.DIRECTIVE_SUBCLASS,
                    LogosElementTypes.DIRECTIVE_GROUP,
                    LogosElementTypes.OBJ_C_METHOD_SCOPE -> {
                        children.add(LogosStructureViewElement(child))
                    }
                }
            }
            child = child.nextSibling
        }

        return children.toTypedArray()
    }
}
