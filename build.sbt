name := """play-slick-example"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies += "com.typesafe.play" %% "play-slick" % "2.0.2"
libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "2.0.2"
libraryDependencies += "com.h2database" % "h2" % "1.4.192"
libraryDependencies += "org.postgresql" % "postgresql" % "9.2-1002-jdbc4"
libraryDependencies += "org.webjars" % "swagger-ui" % "2.1.4"
libraryDependencies += "io.swagger" %% "swagger-play2" % "1.5.3"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.5.10"


libraryDependencies += specs2 % Test

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/maven-releases/"
  

