package com.github.cjwebb.dynamomapper

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient
import com.amazonaws.services.dynamodbv2.model.{AttributeValue, GetItemRequest, PutItemRequest}
import com.amazonaws.services.dynamodbv2.scala.AmazonDynamoDBClient
import org.scalatest.FreeSpec

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.Await

import Formats._

class MainSpec extends FreeSpec {

  "some stuff" - {
    "happens" in {
      val credentials = new BasicAWSCredentials("", "")
      val asyncClient = {
        val c = new AmazonDynamoDBAsyncClient(credentials)
        c.setEndpoint("http://localhost:8000")
        c
      }

      implicit val dynamoFormat = Formats.writeFormat[Product]

      val client = new AmazonDynamoDBClient(asyncClient)

      //  val createTableRequest = {
      //    val req = new CreateTableRequest("products", List(new KeySchemaElement("id", KeyType.HASH)).asJava)
      //    req.setAttributeDefinitions(List(new AttributeDefinition("id", ScalarAttributeType.S)).asJava)
      //    req.setProvisionedThroughput(new ProvisionedThroughput(5L, 5L))
      //    req
      //  }
      //  Await.result(client.createTable(createTableRequest), 10 seconds)

      val putResult = client.putItem(new PutItemRequest("products", Formats.writes(Product("123", "red dress"))))

      //  val putResult = client.putItem(new PutItemRequest("products",
      //    Map("id" -> new AttributeValue("123"),
      //        "name" -> new AttributeValue("hello")).asJava))

      Await.result(putResult, 10 seconds)

      val result = client.getItem(new GetItemRequest("products", Map("id" -> new AttributeValue("123")).asJava))

      val product = Await.result(result, 10 seconds)

      println(product.getItem.asScala)

      client.shutdown()

    }
  }
}

case class Product(id: String, name: String)

object Product {
  implicit val dynamoFormat = Formats.writeFormat[Product]
}

case class Colour(name: String)

object Colour {
  implicit val dynamoFormat = Formats.writeFormat[Colour]
}


