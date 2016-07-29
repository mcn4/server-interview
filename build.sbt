
name := "agile-management"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.1"

resolvers += "Typesafe releases" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
    "org.eclipse.mylyn.github" % "org.eclipse.egit.github.core" % "2.1.5",
    //"com.google.guava" % "guava" % "16.0.1",
    "redis.clients" % "jedis" % "2.1.0",
    javaCore,
    javaJdbc,
    javaEbean
  )

libraryDependencies ++= Seq(
    javaWs
) map (_ % "test")

lazy val root = (project in file(".")).enablePlugins(PlayJava)