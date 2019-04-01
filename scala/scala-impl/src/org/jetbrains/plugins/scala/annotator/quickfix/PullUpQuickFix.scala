package org.jetbrains.plugins.scala.annotator.quickfix

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.{PsiElement, PsiFile}
import org.jetbrains.plugins.scala.annotator.template._
import org.jetbrains.plugins.scala.extensions.PsiElementExt
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScTemplateDefinition

class PullUpQuickFix(element: PsiElement) extends IntentionAction {
  override def getText: String = "Pull up"

  override def getFamilyName: String = "Pull up" // TODO: Clarify

  override def isAvailable(project: Project, editor: Editor, psiFile: PsiFile): Boolean = {
    val clazz = psiFile.findElementAt(editor.getCaretModel.getOffset - 1)
      .parentOfType(classOf[ScTemplateDefinition], strict = false)
      .get // TODO: shortcut

    val refs = superRefs(clazz)

    // TODO: check that we can modify
    true
  }

  override def invoke(project: Project, editor: Editor, psiFile: PsiFile): Unit = {
    //
    ???
  }
}
