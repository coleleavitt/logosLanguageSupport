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
class LogosStructureViewModel(psiFile: PsiFile) : com.intellij.ide.structureView.TextEditorBasedStructureViewModel(psiFile) {

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
class LogosStructureViewElement(private val element: PsiElement) : com.intellij.ide.structureView.StructureViewTreeElement {

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
                return element.text?.take(50)
            }

            override fun getLocationString(): String? {
                return null
            }

            override fun getIcon(unused: Boolean): javax.swing.Icon? {
                return LogosIcons.FILE
            }
        }
    }

    override fun getChildren(): Array<com.intellij.ide.util.treeView.smartTree.TreeElement> {
        // TODO: Implement proper tree structure for hooks/subclasses/methods
        return emptyArray()
    }
}
