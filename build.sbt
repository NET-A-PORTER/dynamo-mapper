name := "dynamomapper"

version := "0.0.1"

scalaVersion := "2.11.7"

scalacOptions ++= Seq("-language:experimental.macros")

libraryDependencies ++=
  "com.amazonaws.scala" % "aws-scala-sdk-dynamodb" % "1.10.7" ::
  "com.typesafe.play" %% "play-json" % "2.4.3" ::
  "org.scalatest" %% "scalatest" % "2.2.4" % "test" ::
  Nil
