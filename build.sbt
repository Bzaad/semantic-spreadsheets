name := """semantic-spreadsheets"""
organization := "com.aucklanduni"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.11"

libraryDependencies += filters
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.5.2" % Test
libraryDependencies += specs2 % Test
libraryDependencies += guice
// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.aucklanduni.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.aucklanduni.binders._"
