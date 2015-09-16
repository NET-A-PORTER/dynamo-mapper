package com.github.cjwebb.dynamomapper

import java.util.UUID

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient
import com.amazonaws.services.dynamodbv2.model._
import com.amazonaws.services.dynamodbv2.scala.AmazonDynamoDBClient
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{Matchers, FreeSpec}

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.Await

import DynamoMapper._

class MainSpec extends FreeSpec with Matchers with ScalaFutures with Fixtures {

  implicit val asyncConfig = PatienceConfig(timeout = scaled(Span(10, Seconds)))

  def newId() = UUID.randomUUID().toString
  val tableName = "table1"

  def createTable(implicit client: AmazonDynamoDBClient) = {
    val createTableRequest = {
      val req = new CreateTableRequest(tableName, List(new KeySchemaElement("id", KeyType.HASH)).asJava)
      req.setAttributeDefinitions(List(new AttributeDefinition("id", ScalarAttributeType.S)).asJava)
      req.setProvisionedThroughput(new ProvisionedThroughput(5L, 5L))
      req
    }
    Await.result(client.createTable(createTableRequest), 10 seconds)
  }

  implicit val client = {
    val credentials = new BasicAWSCredentials("", "")
    val c = new AmazonDynamoDBAsyncClient(credentials)
    c.setEndpoint("http://localhost:8000")
    new AmazonDynamoDBClient(c)
  }

  def putItem(dv: DynamoValue): PutItemResult = {
    client.putItem(new PutItemRequest(tableName, toDynamo(dv))).futureValue
  }

  def getItem(id: String): GetItemResult = {
    client.getItem(new GetItemRequest(tableName, Map("id" -> new AttributeValue(id)).asJava)).futureValue
  }

  "writing objects" - {

    "works with simple string case classes" in {
      val id = newId()
      client.putItem(new PutItemRequest(tableName, toDynamo(SimpleCaseClass(id, "simple")))).futureValue

      val result = client.getItem(new GetItemRequest(tableName, Map("id" -> new AttributeValue(id)).asJava)).futureValue
      val expected = Map("name" -> new AttributeValue("simple"), "id" -> new AttributeValue(id)).asJava

      result.getItem shouldBe expected
    }

    "works with nested case classes" in {
      val id = newId()
      val nestedId = newId()
      client.putItem(new PutItemRequest(tableName, toDynamo(NestedCaseClass(id, SimpleCaseClass(nestedId, "simple"))))).futureValue

      val result = client.getItem(new GetItemRequest(tableName, Map("id" -> new AttributeValue(id)).asJava)).futureValue
      val expected = Map(
        "id" -> new AttributeValue(id),
        "simple" -> new AttributeValue().withM(Map(
          "id" -> new AttributeValue(nestedId),
          "name" -> new AttributeValue("simple")
        ).asJava)
      ).asJava
      result.getItem shouldBe expected

    }

    "works with maps" in {
      val id = newId()
      putItem(ClassWithMap(id, Map("hello" -> "world", "foo" -> "bar")))

      val result = getItem(id)
      val expected = Map(
        "id" -> new AttributeValue(id),
        "map" -> new AttributeValue().withM(Map(
          "hello" -> new AttributeValue("world"),
          "foo" -> new AttributeValue("bar")
        ).asJava)
      ).asJava

      result.getItem shouldBe expected

      client.shutdown()
    }

  }
}
