package org.jetbrains.plugins.scala.findUsages.compilerReferences

import java.util.EventListener

import com.intellij.util.messages.Topic

trait CompilerReferenceServiceStatusListener extends EventListener {
  def onIndexingStarted(): Unit                  = ()
  def onIndexingFinished(success: Boolean): Unit = ()
}

object CompilerReferenceServiceStatusListener {
  val topic: Topic[CompilerReferenceServiceStatusListener] =
    Topic.create[CompilerReferenceServiceStatusListener](
      "compiler reference index build status",
      classOf[CompilerReferenceServiceStatusListener]
    )
}
