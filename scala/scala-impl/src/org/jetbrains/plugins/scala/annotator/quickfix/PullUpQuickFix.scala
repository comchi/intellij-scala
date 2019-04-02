package org.jetbrains.plugins.scala.annotator.quickfix

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.navigation.NavigationUtil
import com.intellij.ide.util.PsiClassListCellRenderer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.ReadonlyStatusHandler
import com.intellij.psi.search.PsiElementProcessor
import com.intellij.psi.{PsiClass, PsiElement, PsiFile}
import org.jetbrains.plugins.scala.ScalaBundle
import org.jetbrains.plugins.scala.annotator.template.superRefs
import org.jetbrains.plugins.scala.extensions.{PsiElementExt, inWriteCommandAction}
import org.jetbrains.plugins.scala.lang.psi.api.statements.{ScPatternDefinition, ScVariableDefinition}
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.ScModifierListOwner
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ScMember, ScTemplateDefinition}
import org.jetbrains.plugins.scala.lang.refactoring.extractTrait.ScalaExtractMemberInfo
import org.jetbrains.plugins.scala.lang.refactoring.memberPullUp.ScalaPullUpProcessor

class PullUpQuickFix(element: PsiElement, memberNameId: PsiElement) extends IntentionAction {

  override def getText: String = element match {
      case _: ScVariableDefinition => ScalaBundle.message("pull.variable.to", memberNameId.getText)
      case _: ScPatternDefinition => ScalaBundle.message("pull.value.to", memberNameId.getText)
      case _ => ScalaBundle.message("pull.method.to", memberNameId.getText)
    }

  override def getFamilyName: String = getText

  override def startInWriteAction(): Boolean = true

  override def isAvailable(project: Project, editor: Editor, psiFile: PsiFile): Boolean = {
    getSelectedElement(classOf[ScTemplateDefinition], editor, psiFile)
      .exists(
        clazz => {
          val writableParentExists = superRefs(clazz).exists(
            ref => ReadonlyStatusHandler
              .ensureFilesWritable(project, ref._2.getContainingFile.getVirtualFile))

          val hasOverrideModifier = getSelectedElement(classOf[ScModifierListOwner], editor, psiFile)
            .get
            .hasModifierProperty("override")

          hasOverrideModifier && writableParentExists
        }
      )
  }

  override def invoke(project: Project, editor: Editor, psiFile: PsiFile): Unit = {
    val sourceClass: ScTemplateDefinition = getSelectedElement(classOf[ScTemplateDefinition], editor, psiFile).get
    val memberToOverride: ScMember = getSelectedElement(classOf[ScMember], editor, psiFile).get
    val superClasses = allSupers(project, sourceClass)

    if (!ApplicationManager.getApplication.isUnitTestMode) {
      NavigationUtil.getPsiElementPopup[PsiClass](
        superClasses.toArray, new PsiClassListCellRenderer, "Choose class", new PsiElementProcessor[PsiClass] {
          override def execute(t: PsiClass): Boolean = {
            ApplicationManager.getApplication.invokeLater(
              () => {
                inWriteCommandAction {
                  pullUpMember(
                    target = memberToOverride,
                    from = sourceClass,
                    to = t,
                    project = project
                  )
                }(project)
              })
            true
          }
        }).showInBestPositionFor(editor)
    } else {
      // for headless test flow
      superClasses.headOption.foreach(pullUpMember(memberToOverride, sourceClass, _: PsiClass, project))
    }
  }

  private def pullUpMember(target: ScMember, from: ScTemplateDefinition, to: PsiClass, project: Project): Unit = {
    val info = new ScalaExtractMemberInfo(target)
    info.setToAbstract(true)
    new ScalaPullUpProcessor(project, from, to.asInstanceOf[ScTemplateDefinition], Seq(info))
      .moveMembersToBase()
  }

  private def getSelectedElement[T <: PsiElement](clazz: Class[T], editor: Editor, psiFile: PsiFile): Option[T] =
    psiFile.findElementAt(editor.getSelectionModel.getSelectionStart)
      .parentOfType[T](clazz, strict = false)

  private def allSupers(project: Project, clazz: PsiClass): Set[PsiClass] = {
    val supers = clazz.getSupers
      .filter(c => ReadonlyStatusHandler.ensureFilesWritable(project, c.getContainingFile.getVirtualFile))
    (supers ++ supers.flatMap(c => allSupers(project, c))).toSet
  }

}
