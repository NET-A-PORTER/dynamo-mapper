package com.netaporter.dynamomapper

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.netaporter.dynamomapper.DynamoMapper._

import scala.collection.JavaConverters._
import scala.annotation.implicitNotFound

/**
 * Represents a type of value in Dynamo.
 * These are represented in the Dynamo API as S, N, M etc...
 */
trait DynamoValue
case class DynamoString(s: String) extends DynamoValue
case class DynamoMap(m: Map[String, DynamoValue]) extends DynamoValue
case class DynamoList(l: Seq[DynamoValue]) extends DynamoValue
// todo - add the rest of them

/**
 * Base trait that needs to be implemented for any T that needs writing to DynamoDB.
 * Typically, this is an implicit.
 *
 * @tparam T the type to write to DynamoDB
 */
@implicitNotFound("No DynamoWrites serializer found for type ${T}. Try to implement one.")
trait DynamoWrites[T] {
  def writes(o: T): DynamoValue
}

object DynamoMapper extends DefaultDynamoWrites {
  /**
   * The type expected by the Java SDK for most 'Item' operations
   */
  type DynamoData = java.util.Map[String, AttributeValue]

  implicit def writes[T](o: T)(implicit w: DynamoWrites[T]): DynamoValue = w.writes(o)

  /**
   * Creates a DynamoWrites[T] by resolving case class fields and values,
   * and required implicits are compile-time.
   *
   * {{{
   *   import DynamoMapper._
   *
   *   case class User(name: String, email: String)
   *
   *   implicit val userWrites = DynamoMapper.writeFormat[User]
   *   // macro-compiler will expand this and is equivalent to:
   *   implicit val userWrites = new DynamoWrites[User] {
   *     override def writes(o: User) = map("name" -> o.name, "email" -> o.email)
   *   }
   * }}}
   */
  def writeFormat[T]: DynamoWrites[T] = macro FormatsMacroImpl.formatWriteImpl[T]

  /**
   * To make chaining of formats nicer
   */
  sealed trait DynamoValueWrapper
  private case class DynamoValueWrapperImpl(field: DynamoValue) extends DynamoValueWrapper

  implicit def toDynamoValueWrapper[T](field: T)(implicit w: DynamoWrites[T]): DynamoValueWrapper =
    DynamoValueWrapperImpl(w.writes(field))

  def map(fields: (String, DynamoValueWrapper)*): DynamoMap =
    DynamoMap(fields.map(f => (f._1, f._2.asInstanceOf[DynamoValueWrapperImpl].field)).toMap)

  /**
   * Method to finally convert our representation of data into the form that the Java SDK needs
   * @throws IllegalArgumentException if not given a [[DynamoMap]], as thats is what the Java SDK
   *                                  requires at the top-level.
   */
  def toDynamo(d: DynamoValue): DynamoData = {
    def convert(value: DynamoValue): AttributeValue = {
      value match {
        case DynamoString(s) => new AttributeValue(s)
        case DynamoMap(m) => new AttributeValue().withM(m.mapValues(convert).asJava)
        case DynamoList(l) => new AttributeValue().withL(l.map(convert).asJava)
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

  implicit def mapWritesT[T](implicit w: DynamoWrites[T]) = new DynamoWrites[Map[String, T]] {
    override def writes(o: Map[String, T]): DynamoValue = {
      DynamoMap(o.mapValues(v => w.writes(v)))
    }
  }

  implicit def seqWritesT[T](implicit w: DynamoWrites[T]) = new DynamoWrites[Seq[T]] {
    override def writes(o: Seq[T]): DynamoValue = {
      DynamoList(o.map(v => w.writes(v)))
    }
  }
  // todo - add the rest of them
}
