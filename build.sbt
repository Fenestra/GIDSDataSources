name := """DataSources"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

//unmanagedSourceDirectories in Compile += file("/home/lee/PlayProjects/CommonWestat")
unmanagedSourceDirectories in Compile += file("c:/sandbox/CommonWestat")

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "mysql" % "mysql-connector-java" % "5.1.34",
  "com.typesafe.play" %% "anorm" % "2.5.1",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)

