import sbt._
import sbt.Keys._
import com.teambytes.sbt.dynamodb.DynamoDBLocal
import com.teambytes.sbt.dynamodb.DynamoDBLocal.Keys._

object localdynamodb {

  val settings: Seq[Setting[_]] = DynamoDBLocal.settings ++ Seq(
    dynamoDBLocalDownloadDirectory := file("dynamodb-local"),
    test in Test <<= (test in Test).dependsOn(startDynamoDBLocal),
    dynamoDBLocalInMemory := true,

    // Plugin currently downloads "latest" every single time it runs, unless you specify a version.
    // We need this PR merged: https://github.com/grahamar/sbt-dynamodb/pull/4
    dynamoDBLocalVersion := "2015-07-16_1.0"
  )

}
