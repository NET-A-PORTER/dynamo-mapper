package com.netaporter.dynamomapper

/**
 * Represents a type of value in Dynamo.
 * These are represented in the Dynamo API as S, N, M etc...
 *
 * Look at the DynamoDB Documentation for more information.
 */
trait DynamoValue extends DynamoReadable {
  def as[T](implicit reads: DynamoReads[T]): DynamoReadResult[T] = reads.reads(this)
}

case class DynamoString(s: String) extends DynamoValue

case class DynamoNumber(s: String) extends DynamoValue

case class DynamoMap(m: Map[String, DynamoValue]) extends DynamoValue

case class DynamoList(l: Seq[DynamoValue]) extends DynamoValue

case class DynamoStringSet(s: Set[String]) extends DynamoValue

// todo - DynamoNumberSet

// todo - DynamoBinarySet

case object DynamoNull extends DynamoValue
