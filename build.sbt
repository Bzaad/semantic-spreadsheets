name := """semantic-spreadsheets"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

val akkaVersion = "2.4.2"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "org.webjars" %% "webjars-play" % "2.5.0",
  "org.webjars" % "requirejs" % "2.3.2",
  "org.webjars" % "bootstrap" % "3.2.0",
  "org.webjars" % "jquery" % "3.1.1-1",
  "org.webjars" % "handsontable" % "0.26.0",
  "org.webjars" % "SlickGrid" % "2.1",
  "com.typesafe.akka" %% "akka-actor" % "2.3.4",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.4",
  "org.webjars" % "flot" % "0.8.0",
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
