name := """lunatech-blog-engine"""
organization := "com.lunatech.blog"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.8"

libraryDependencies ++= Seq(
  guice, ws, ehcache,
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
  "org.asciidoctor" % "asciidoctorj" % "2.5.5",
  "com.47deg" %% "github4s" % "0.31.1",
  "com.typesafe.play" %% "play-json" % "2.9.2",
)

Global / onChangedBuildSource := ReloadOnSourceChanges
