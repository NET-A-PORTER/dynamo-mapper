package com.github.cjwebb.dynamomapper

import com.amazonaws.services.dynamodbv2.model._
import com.github.cjwebb.dynamomapper.DynamoMapper._
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}

import scala.collection.JavaConverters._

class MainSpec extends FreeSpec with Matchers with ScalaFutures with Fixtures with DynamoDBClientMixin {

  implicit val asyncConfig = PatienceConfig(timeout = scaled(Span(10, Seconds)))

  def putItem(dv: DynamoValue): PutItemResult = {
    client.putItem(new PutItemRequest(tableName, toDynamo(dv))).futureValue
  }

  def getItem(id: String): GetItemResult = {
    client.getItem(new GetItemRequest(tableName, Map("id" -> new AttributeValue(id)).asJava)).futureValue
  }

  "writing objects" - {

    "toDynamo throws IllegalArgumentException if not given a DynamoMap" in {
      intercept[IllegalArgumentException] (
        toDynamo(DynamoString("s"))
      )
    }

    "works with simple string case classes" in {
      val id = newId()

      putItem(SimpleCaseClass(id, "simple"))

      val result = getItem(id)
      val expected = Map("name" -> new AttributeValue("simple"), "id" -> new AttributeValue(id)).asJava

      result.getItem shouldBe expected
    }

    "works with nested case classes" in {
      val id = newId()
      val nestedId = newId()

      putItem(NestedCaseClass(id, SimpleCaseClass(nestedId, "simple")))

      val result = getItem(id)
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
    }

  }
}
