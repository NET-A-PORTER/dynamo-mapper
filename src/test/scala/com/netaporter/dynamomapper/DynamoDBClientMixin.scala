package com.netaporter.dynamomapper

import java.util.UUID

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient
import com.amazonaws.services.dynamodbv2.model._
import com.amazonaws.services.dynamodbv2.scala.AmazonDynamoDBClient
import org.scalatest.{BeforeAndAfterAll, Suite}

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration._

trait DynamoDBClientMixin extends BeforeAndAfterAll { this: Suite =>
  def newId() = UUID.randomUUID().toString
  val tableName = "table1"

  implicit lazy val client = {
    val credentials = new BasicAWSCredentials("", "")
    val c = new AmazonDynamoDBAsyncClient(credentials)
    c.setEndpoint("http://localhost:8000")
    new AmazonDynamoDBClient(c)
  }

  private def createTable()(implicit client: AmazonDynamoDBClient) = {
    val createTableRequest = {
      val req = new CreateTableRequest(tableName, List(new KeySchemaElement("id", KeyType.HASH)).asJava)
      req.setAttributeDefinitions(List(new AttributeDefinition("id", ScalarAttributeType.S)).asJava)
      req.setProvisionedThroughput(new ProvisionedThroughput(5L, 5L))
      req
    }
    Await.result(client.createTable(createTableRequest), 10 seconds)
  }

  override def beforeAll() = {
    createTable()
  }

  override def afterAll() = {
    client.shutdown()
  }

}
