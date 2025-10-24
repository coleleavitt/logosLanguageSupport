package com.coleleavitt.logos.intellij

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

/**
 * Basic PSI element for Logos language.
 *
 * This is a simple wrapper around AST nodes for the lexer-only approach.
 * For a full parser implementation, this would be extended with specific
 * PSI elements for each Logos construct (HookPsiElement, SubclassPsiElement, etc.)
 */
class LogosPsiElement(node: ASTNode) : ASTWrapperPsiElement(node)
