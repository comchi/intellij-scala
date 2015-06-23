import sbt._


object Dependencies {
  val sbtStructureVersion = "4.1.0"
  val sbtStructureCore = "org.jetbrains" % "sbt-structure-core_2.11" % sbtStructureVersion
  val sbtStructureExtractor012 = "org.jetbrains" % "sbt-structure-extractor-0-12" % sbtStructureVersion
  val sbtStructureExtractor013 = "org.jetbrains" % "sbt-structure-extractor-0-13" % sbtStructureVersion

  val sbtLaunch = "org.scala-sbt" % "sbt-launch" % "0.13.8"
}