ThisBuild / scalaVersion := "3.3.4"
ThisBuild / organization := "com.olafura"
ThisBuild / licenses := Seq(
  "Apache-2.0" -> url("https://opensource.org/license/apache-2-0")
)
ThisBuild / developers += Developer(
  name = "Olafur Arason",
  email = "olafura@olafura.com",
  id = "olafura",
  url = url("http://github.com/olafura/")
)

lazy val core = (crossProject(JSPlatform, JVMPlatform, NativePlatform).crossType(CrossType.Pure) in file("core"))
  .settings(
    name := "core"
  )
  .jvmSettings(
    scalaVersion := "3.3.4",
    libraryDependencies += "org.playframework" %% "play-json" % "3.0.4",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test
  )
  .jsSettings(
    scalaVersion := "3.3.4",
    libraryDependencies += "org.playframework" %%% "play-json" % "3.0.4",
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.19" % Test
  )
  .nativeSettings(
    scalaVersion := "3.3.4",
    libraryDependencies += "com.typesafe.play" %%% "play-json" % "2.10.6",
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.17" % Test
  )
