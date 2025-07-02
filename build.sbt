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

lazy val core = (projectMatrix in file("."))
  .settings(
    name := "json-diff-scala"
  )
  .jvmPlatform(
    scalaVersions = Seq("3.3.4"),
    settings = Seq(
      libraryDependencies += "org.playframework" %% "play-json" % "3.0.4",
      libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test
    )
  )
  .jsPlatform(
    scalaVersions = Seq("3.3.4"),
    settings = Seq(
      libraryDependencies += "org.playframework" %%% "play-json" % "3.0.4",
      libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.19" % Test
    )
  )
  .nativePlatform(
    scalaVersions = Seq("3.3.4"),
    settings = Seq(
      libraryDependencies += "com.typesafe.play" %%% "play-json" % "2.10.6",
      libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.17" % Test
    )
  )
