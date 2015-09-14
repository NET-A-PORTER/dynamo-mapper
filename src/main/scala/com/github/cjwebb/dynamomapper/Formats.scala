package com.github.cjwebb.dynamomapper

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.github.cjwebb.dynamomapper.Formats._

import scala.collection.JavaConverters._
import scala.annotation.implicitNotFound

/**
 * Represents a type of value in Dynamo.
 * These are represented in the Dynamo API as S, N, M etc...
 */
trait DynamoValue
case class DynamoString(s: String) extends DynamoValue
case class DynamoMap(m: Map[String, DynamoValue]) extends DynamoValue
// todo - add the rest of them

object DynamoMap {
  def apply(fields: Seq[(String, DynamoValue)]): DynamoMap = DynamoMap(fields.toMap)
}

object Formats extends DefaultDynamoWrites {

  /**
   * The type expected by the Java SDK for most 'Item' operations
   */
  type DynamoData = java.util.Map[String, AttributeValue]

  @implicitNotFound("No DynamoWrites serializer found for type ${T}. Try to implement one.")
  trait DynamoWrites[T] {
    def writes(o: T): DynamoValue
  }

  implicit def writes[T](o: T)(implicit w: DynamoWrites[T]): DynamoValue = w.writes(o)

  //def writeFormat[T] = macro FormatsMacroImpl.formatWriteImpl[T]

  /**
   * To make chaining of formats nicer
   */
  sealed trait DynamoValueWrapper
  private case class DynamoValueWrapperImpl(field: DynamoValue) extends DynamoValueWrapper

  implicit def toDynamoValueWrapper[T](field: T)(implicit w: DynamoWrites[T]): DynamoValueWrapper =
    DynamoValueWrapperImpl(w.writes(field))

  def map(fields: (String, DynamoValueWrapper)*): DynamoMap =
    DynamoMap(fields.map(f => (f._1, f._2.asInstanceOf[DynamoValueWrapperImpl].field)))

  // todo - fix this method - needs to compile-time check rather than throw exception
  def toDynamo(d: DynamoValue): DynamoData = {
    def convert(value: DynamoValue): AttributeValue = {
      value match {
        case DynamoString(s) => new AttributeValue(s)
        case DynamoMap(m) => new AttributeValue().withM(m.mapValues(convert).asJava)
      }
    }
    d match {
      case DynamoMap(m) => m.mapValues(convert).asJava
      case _ => throw new IllegalArgumentException("top level object must be a Dynamo Map")
    }
  }
}

trait DefaultDynamoWrites {
  implicit object StringWrites extends DynamoWrites[String] {
    override def writes(s: String): DynamoValue = DynamoString(s)
  }

  implicit object MapWrites extends DynamoWrites[Map[String, DynamoValue]] {
    override def writes(m: Map[String, DynamoValue]) = DynamoMap(m)
  }
}
