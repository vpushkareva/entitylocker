ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.7"

enablePlugins(JavaAppPackaging)


lazy val root = (project in file("."))
  .settings(
    name := "httpscala"
  )

libraryDependencies += ("com.typesafe.akka" %% "akka-http" % "10.2.6")

libraryDependencies += ("com.typesafe.akka" %% "akka-stream" % "2.6.17")

libraryDependencies += "com.github.cb372" %% "scalacache-guava" % "0.28.0"

libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % "2.6.17"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.10"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.10" % "test"

libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % "10.2.7" % Test

libraryDependencies +=  "org.scalaj" %% "scalaj-http" % "2.4.2"