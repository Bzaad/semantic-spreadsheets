name := """semantic-spreadsheets"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "org.webjars" %% "webjars-play" % "2.5.0",
  "org.webjars" % "requirejs" % "2.3.2",
  "org.webjars" % "jquery" % "3.1.1-1",
  "org.webjars" % "bootstrap" % "3.1.1-2",
  "org.webjars" % "handsontable" % "0.26.0"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
