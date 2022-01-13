import sbt._

object Dependencies {

  import Versions._

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.9"
  lazy val caliban = "com.github.ghostdogpr" %% "caliban" % CalibanVersion
  lazy val calibanFederation = "com.github.ghostdogpr" %% "caliban-federation" % CalibanVersion
  lazy val calibanAkkaHttp = "com.github.ghostdogpr" %% "caliban-akka-http" % CalibanVersion
  lazy val calibanPlay = "com.github.ghostdogpr" %% "caliban-play" % CalibanVersion
  lazy val slf4j = "org.slf4j" % "slf4j-simple" % "1.7.32"
}

object Versions {
  val CalibanVersion = "1.3.2"
}
