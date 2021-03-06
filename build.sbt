name := """semantic-spreadsheets"""
organization := "com.aucklanduni"

version := "1.0-SNAPSHOT"

/**
  * rjs is the require js optimizer for sbt. although we do not use requirejs internally it comes included with WebJars
  * not including it known to cause dependency injectrion issues. however, further investigation is necessary to determine
  * if it would be possible ro remove it without causing dependency injection related issues.
*/

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb)

routesGenerator := InjectedRoutesGenerator

scalaVersion := "2.11.12"


libraryDependencies += filters
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.5.2" % Test
libraryDependencies += specs2 % Test
libraryDependencies += guice


//WebJars Dependencies
libraryDependencies += "org.webjars" %% "webjars-play" % "2.6.0"
libraryDependencies += "org.webjars.bower" % "jquery" % "3.3.1"
libraryDependencies += "org.webjars" % "jquery-ui" % "1.12.1"
libraryDependencies += "org.webjars" % "bootstrap" % "3.3.7-1"
libraryDependencies += "org.webjars.bower" % "lodash" % "4.17.4"
libraryDependencies += "org.webjars" % "momentjs" % "2.18.1"
libraryDependencies += "org.webjars.bower" % "papaparse" % "4.3.6"
libraryDependencies += "org.webjars.bower" % "filesaver" % "1.3.3"
libraryDependencies += "org.webjars" % "jszip" % "3.1.0"
libraryDependencies += "org.webjars.bower" % "bootstrap-select" % "1.12.4"
