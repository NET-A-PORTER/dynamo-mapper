name := "dynamomapper"

organization  := "com.netaporter"

version := "0.1.0-SNAPSHOT"

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
  "org.scalacheck" %% "scalacheck" % "1.12.5" % "test" ::
  Nil

// use DynamoDBLocal for testing
localdynamodb.settings

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases"

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomExtra := (
  <url>https://github.com/net-a-porter/dynamo-mapper</url>
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:net-a-porter/dynamo-mapper.git</url>
      <connection>scm:git@github.com:net-a-porter/dynamo-mapper.git</connection>
    </scm>
    <developers>
      <developer>
        <id>cjwebb</id>
        <name>Colin J Webb</name>
        <url>http://cjwebb.github.io</url>
      </developer>
    </developers>)
