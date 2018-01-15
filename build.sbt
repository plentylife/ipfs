name := "ipfs"

version := "0.1"

scalaVersion := "2.12.4"

lazy val model = project.enablePlugins(ScalaJSPlugin, SbtJsEngine).in(file("model"))
  .settings(libraryDependencies ++= Seq(
    "com.lihaoyi" %% "upickle" % "0.5.1"
  ))

lazy val ui = project.enablePlugins(ScalaJSPlugin, SbtJsEngine).in(file("ui"))
  //  .enablePlugins(ScalaJSBundlerPlugin)
  .dependsOn(model).settings(
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
  //  webpackBundlingMode := BundlingMode.LibraryOnly(),
//  scalaJSUseMainModuleInitializer := false,
//  scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
  libraryDependencies ++= Seq(
    "com.lihaoyi" %% "upickle" % "0.5.1",
    "com.thoughtworks.binding" %%% "dom" % "latest.release",
    "com.thoughtworks.binding" %%% "route" % "11.0.1"),
  //  npmDependencies in Compile += "ipfs" -> "0.27.6",
  //  npmDependencies in Compile += "orbit-db" -> "0.19.2",

)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

