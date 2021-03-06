package org.jetbrains.plugins.scala.codeInspection.collections

import com.intellij.codeInsight.PsiEquivalenceUtil.areElementsEquivalent
import org.jetbrains.plugins.scala.codeInspection.InspectionBundle
import org.jetbrains.plugins.scala.codeInspection.collections.IndexBoundsCheckInspection._
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScExpression

/**
  * @author t-kameyama
  */
class IndexBoundsCheckInspection extends OperationOnCollectionInspection {
  override def possibleSimplificationTypes: Array[SimplificationType] = Array(IndexBoundsCheck)
}

private object IndexBoundsCheckInspection {

  private val IndexBoundsCheck = new SimplificationType {

    override def hint: String = InspectionBundle.message("ifstmt.to.lift")

    override def getSimplification(expr: ScExpression): Option[Simplification] = expr match {
      case IfStmt(IndexLessThanLengthCondition(seq1, index1), Then(seq2, index2), scalaNone())
        if areElementsEquivalent(seq1, seq2) && areElementsEquivalent(index1, index2) =>
        createSimplification(expr, seq1, index1)

      case IfStmt(IndexGreaterEqualsLengthCondition(seq1, index1), scalaNone(), Then(seq2, index2))
        if areElementsEquivalent(seq1, seq2) && areElementsEquivalent(index1, index2) =>
        createSimplification(expr, seq1, index1)

      case _ => None
    }

    private def createSimplification(expr: ScExpression, seq: ScExpression, index: ScExpression): Option[Simplification] = {
      Some(replace(expr).withText(invocationText(seq, "lift", index)).highlightFrom(expr))
    }

    private object IndexLessThanLengthCondition {
      def unapply(expr: ScExpression): Option[(ScExpression, ScExpression)] = expr match {
        case index `<` seq `.sizeOrLength` () if isSeq(seq) => Some(seq, index)
        case seq `.sizeOrLength` () `>` index if isSeq(seq) => Some(seq, index)
        case _ => None
      }
    }

    private object IndexGreaterEqualsLengthCondition {
      def unapply(expr: ScExpression): Option[(ScExpression, ScExpression)] = expr match {
        case index `>=` seq `.sizeOrLength` () if isSeq(seq) => Some(seq, index)
        case seq `.sizeOrLength` () `<=` index if isSeq(seq) => Some(seq, index)
        case _ => None
      }
    }

    private object Then {
      def unapply(expr: ScExpression): Option[(ScExpression, ScExpression)] = expr match {
        case scalaSome(seq `.apply` (index)) if isSeq(seq) => Some(seq, index)
        case _ => None
      }
    }

  }

}
