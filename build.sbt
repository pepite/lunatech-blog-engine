name := """lunatech-blog-engine"""
organization := "com.lunatech.blog"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, DockerPlugin)

scalaVersion := "2.12.10"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.1" % Test
libraryDependencies += "org.asciidoctor" % "asciidoctorj" % "2.2.0"
libraryDependencies += "com.47deg" %% "github4s" % "0.20.1"
libraryDependencies += ws
libraryDependencies += ehcache

javaOptions in Universal ++= Seq("-Dconfig.file=conf/production.conf")

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.lunatech.blog.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.lunatech.blog.binders._"
