import cbt._
class Build(val context: Context) extends ScalaJsBuild {
  override def name = "quiz"

  override def sources =
    super.sources ++ Seq(projectDirectory.getParentFile ++ "/shared")

  override def dependencies = (
    super.dependencies ++
      Resolver(mavenCentral).bind(
        "com.thoughtworks.binding" %%% "dom" % "latest.release"
      )
  )

  override def scalaJsTargetFile =
    projectDirectory.getParentFile ++ ("/jvm/public/generated/" ++ name ++ ".js")
}
