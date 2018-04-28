val circeVersion = "0.9.3"
val doobieVersion = "0.5.2"

val scalaV = "2.12.5"

import java.nio.file.Files

lazy val copyJs = TaskKey[Unit]("copyJs")

lazy val root = (project in file(".")).aggregate(jvm, js).disablePlugins(RevolverPlugin)

lazy val jvm = (project in file("jvm")).settings(
  scalaVersion := scalaV,
  libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.0.11",
      "com.typesafe.akka" %% "akka-stream" % "2.5.11",
      "com.typesafe.akka" %% "akka-actor-typed" % "2.5.11",
      "com.typesafe.akka" %% "akka-stream-typed" % "2.5.11",
      "com.softwaremill.akka-http-session" %% "core" % "0.5.4",

      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-generic-extras" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "de.heikoseeberger" %% "akka-http-circe" % "1.19.0",

      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-postgres"  % doobieVersion,

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
  (Compile / compile) := ((Compile / compile) dependsOn (fastOptJS in (js, Compile))).value,
  resources in Compile += (fastOptJS in(js, Compile)).value.data,
  mainClass in reStart := Some("quiz.Main")
).dependsOn(sharedJVM, js)

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
