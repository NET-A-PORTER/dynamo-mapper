name := "dynamomapper"

version := "0.0.1"

scalaVersion := "2.11.7"

scalacOptions ++=
  "-language:experimental.macros" ::
  "-language:implicitConversions" ::
  "-language:postfixOps" ::
  "-feature" ::
  "-deprecation" ::
  "-unchecked" ::
  Nil

libraryDependencies ++=
  "com.amazonaws.scala" % "aws-scala-sdk-dynamodb" % "1.10.7" ::
  "org.scala-lang" % "scala-reflect" % scalaVersion.value ::
  "org.scalatest" %% "scalatest" % "2.2.4" % "test" ::
  Nil

// use DynamoDBLocal for testing
localdynamodb.settings
