import cbt._
class Build(val context: Context) extends BaseBuild {

  val circeVersion = "0.9.1"
  val doobieVersion = "0.5.0"

  override def defaultScalaVersion = "2.12.5"

  override def resourceClasspath = super.resourceClasspath ++ ClassPath(
    Seq(projectDirectory ++ "/src/main/resources")
  )

  override def sources =
    super.sources ++ Seq(projectDirectory.getParentFile ++ "/shared")

  override def dependencies = {
    super.dependencies ++
      Resolver(mavenCentral).bind(
        "com.typesafe.akka" %% "akka-http" % "10.0.11",
        "com.typesafe.akka" %% "akka-stream" % "2.5.11",
        "com.typesafe.akka" %% "akka-actor-typed" % "2.5.11",
        "com.typesafe.akka" %% "akka-stream-typed" % "2.5.11",
        "io.circe" %% "circe-core" % circeVersion,
        "io.circe" %% "circe-generic" % circeVersion,
        "io.circe" %% "circe-generic-extras" % circeVersion,
        "io.circe" %% "circe-parser" % circeVersion,
        "de.heikoseeberger" %% "akka-http-circe" % "1.19.0",
        "org.tpolecat" %% "doobie-core" % doobieVersion,
        "org.tpolecat" %% "doobie-postgres" % doobieVersion,
        "org.mindrot" % "jbcrypt" % "0.4"
      )
  }
}
