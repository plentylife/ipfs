import org.scalajs.sbtplugin.ScalaJSPlugin.AutoImport.emitSourceMaps

name := "ipfs"

version := "0.1"

scalaVersion := "2.12.4"

lazy val data = project.enablePlugins(ScalaJSPlugin, SbtJsEngine).in(file("data"))
  .dependsOn(model)
  .settings(
    relativeSourceMaps := true,
    emitSourceMaps := true
  )

lazy val model = project.enablePlugins(ScalaJSPlugin).in(file("model"))
  .settings(
    relativeSourceMaps := true,
    emitSourceMaps := true,
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "scalarx" % "0.3.2"
    )
  )

lazy val ui = project.enablePlugins(ScalaJSPlugin, SbtJsEngine).in(file("ui"))
  .dependsOn(model, data).settings(
  relativeSourceMaps := true,
  emitSourceMaps := true,
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
  libraryDependencies ++= Seq(
    //    "com.lihaoyi" %% "upickle" % "0.5.1",
    "com.thoughtworks.binding" %%% "dom" % "latest.release",
    "com.thoughtworks.binding" %%% "route" % "11.0.1"),
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

