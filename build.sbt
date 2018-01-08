name := "ipfs"

version := "0.1"

scalaVersion := "2.12.4"

lazy val model = project.enablePlugins(ScalaJSPlugin, SbtJsEngine).in(file("model"))
lazy val ui = project.enablePlugins(ScalaJSPlugin, SbtJsEngine).in(file("ui"))
  .dependsOn(model).settings(
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
//  scalaJSUseMainModuleInitializer := false,
//  scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
  libraryDependencies ++= Seq(
    "com.thoughtworks.binding" %%% "dom" % "latest.release",
    "com.thoughtworks.binding" %%% "route" % "11.0.1")
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

