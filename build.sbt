val http4sVersion = "0.18.11"
val circeVersion = "0.9.3"
val doobieVersion = "0.5.2"
val tsecVersion = "0.0.1-M11"

val scalaV = "2.12.6"

scalaVersion in ThisBuild := scalaV

import java.nio.file.Files

lazy val copyJs = TaskKey[Unit]("copyJs")

lazy val root = (project in file(".")).aggregate(jvm, js).disablePlugins(RevolverPlugin)

lazy val jvm = (project in file("jvm")).settings(
  scalaVersion := scalaV,
  scalacOptions := Seq(
    "-Ypartial-unification",
  ),
  libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,

      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-generic-extras" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,

      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-postgres"  % doobieVersion,

      "io.github.jmcardon" %% "tsec-common" % tsecVersion,
      "io.github.jmcardon" %% "tsec-password" % tsecVersion,
      "io.github.jmcardon" %% "tsec-jwt-mac" % tsecVersion,
      "io.github.jmcardon" %% "tsec-jwt-sig" % tsecVersion,
      "io.github.jmcardon" %% "tsec-http4s" % tsecVersion,

      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",

      "org.mindrot" % "jbcrypt" % "0.4",

      "com.lihaoyi" %% "utest" % "0.6.3" % "test"
  ),
  copyJs := {
    fastOptJS in (js, Compile)
    new File("js/target/scala-2.12/*.js").listFiles().foreach(
      file => Files.copy(file.toPath, new File("jvm/target/scala-2.12/classes").toPath)
    )
  },
  //(Compile / compile) := ((Compile / compile) dependsOn (fastOptJS in (js, Compile))).value,
  //resources in Compile += (fastOptJS in(js, Compile)).value.data,
  mainClass in reStart := Some("quiz.Main"),
  testFrameworks += new TestFramework("utest.runner.Framework")
).dependsOn(sharedJVM)

lazy val js = (project in file("js")).settings(
  scalaVersion := scalaV,
  scalacOptions := Seq(
    "-Xxml:coalescing"
  ),
  libraryDependencies ++= Seq(
    "com.thoughtworks.binding" %%% "dom" % "latest.release",
    "com.thoughtworks.binding" %%% "route" % "latest.release",
    "fr.hmil" %%% "roshttp" % "2.1.0",
    "io.circe" %%% "circe-core" % circeVersion,
    "io.circe" %%% "circe-generic" % circeVersion,
    "io.circe" %%% "circe-generic-extras" % circeVersion,
    "io.circe" %%% "circe-parser" % circeVersion,
  ),
  scalaJSUseMainModuleInitializer := true,
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
).enablePlugins(ScalaJSPlugin).dependsOn(sharedJS).disablePlugins(RevolverPlugin)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).settings(
  scalaVersion := scalaV,
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core" % "1.0.1",
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-generic-extras" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion,
  ),
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
)

lazy val sharedJVM = shared.jvm
lazy val sharedJS = shared.js
