addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.0.6")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.22")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")

addSbtPlugin("ch.epfl.scala" % "sbt-bloop" % "1.0.0-M9")

addCompilerPlugin(
  "org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
