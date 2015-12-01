package com.netaporter.dynamomapper

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.netaporter.dynamomapper.DynamoMapper._

import scala.collection.JavaConverters._
import scala.annotation.implicitNotFound

trait DynamoReadable {
  def as[T](implicit reads: DynamoReads[T]): DynamoReadResult[T]
}

/**
 * Represents a type of value in Dynamo.
 * These are represented in the Dynamo API as S, N, M etc...
 */
trait DynamoValue extends DynamoReadable {
  def as[T](implicit reads: DynamoReads[T]): DynamoReadResult[T] = reads.reads(this)
}

case class DynamoString(s: String) extends DynamoValue

case class DynamoMap(m: Map[String, DynamoValue]) extends DynamoValue

case class DynamoList(l: Seq[DynamoValue]) extends DynamoValue

case object DynamoNull extends DynamoValue

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

object DynamoMapper extends DefaultDynamoWrites with DefaultDynamoReads {
  /**
   * The type expected by the Java SDK for most 'Item' operations
   */
  type DynamoData = java.util.Map[String, AttributeValue]

  implicit def writes[T](t: T)(implicit w: DynamoWrites[T]): DynamoValue = w.writes(t)

  implicit def reads[T](v: DynamoValue)(implicit r: DynamoReads[T]): DynamoReadResult[T] = r.reads(v)

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

  def readFormat[T]: DynamoReads[T] = macro FormatsMacroImpl.formatReadImpl[T]
  /**
   * To make chaining of formats nicer
   */
  sealed trait DynamoValueWrapper

  private case class DynamoValueWrapperImpl(field: DynamoValue) extends DynamoValueWrapper

  implicit def toDynamoValueWrapper[T](field: T)(implicit w: DynamoWrites[T]): DynamoValueWrapper =
    DynamoValueWrapperImpl(w.writes(field))

  def map(fields: (String, DynamoValueWrapper)*): DynamoMap =
    DynamoMap(fields.map(f => (f._1, f._2.asInstanceOf[DynamoValueWrapperImpl].field)).toMap)

  implicit class DynamoPath(val value: DynamoValue) extends AnyVal {
    def attr[T](name: String)(implicit reads: DynamoReads[T]): DynamoReadResult[T] = value match {
      // todo - make this nicer
      case DynamoMap(m) => m.getOrElse(name, DynamoNull) match {
        case n @ DynamoNull => n.as[T] match {
          case DynamoReadFailure(errs) => DynamoReadFailure(Seq("not found: " + name))
          case x => x
        }
        case x => x.as[T]
      }
      case x => DynamoReadFailure(Seq(s"$x is not an instance of DynamoMap"))
    }
  }

  /**
   * Method to finally convert our representation of data into the form that the Java SDK needs
   * @throws IllegalArgumentException if not given a [[DynamoMap]], as thats is what the Java SDK
   *                                  requires at the top-level.
   */
  def toDynamo(d: DynamoValue): DynamoData = {
    def filterNulls(m: Map[String, DynamoValue]) = m.filter{ case (k, v) => v != DynamoNull }

    def convert(value: DynamoValue): AttributeValue = {
      value match {
        case DynamoString(s) => new AttributeValue(s)
        case DynamoMap(m) => new AttributeValue().withM(filterNulls(m).mapValues(convert).asJava)
        case DynamoList(l) => new AttributeValue().withL(l.map(convert).asJava)
      }
    }
    d match {
      case DynamoMap(m) => filterNulls(m).mapValues(convert).asJava
      case _ => throw new IllegalArgumentException("top level object must be a Dynamo Map")
    }
  }

  def fromDynamo(data: DynamoData): DynamoValue = {
    def convert(a: AttributeValue): DynamoValue = {
      def isM: Boolean = Option(a.getM).isDefined
      def isL: Boolean = Option(a.getL).isDefined

      if (isM) DynamoMap(a.getM.asScala.mapValues(convert).toMap)
      else if (isL) DynamoList(a.getL.asScala.map(convert).toList)
      else DynamoString(a.getS)
    }

    DynamoMap(data.asScala.mapValues(convert).toMap)
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

  implicit def writesOptionT[T](implicit w: DynamoWrites[T]) = new DynamoWrites[Option[T]] {
    override def writes(o: Option[T]): DynamoValue = {
      o.map(t => w.writes(t)).getOrElse(DynamoNull)
    }
  }
  // todo - add the rest of them
}

trait DefaultDynamoReads {

  implicit object StringReads extends DynamoReads[String] {
    override def reads(value: DynamoValue): DynamoReadResult[String] = {
      value match {
        case DynamoString(s) => DynamoReadSuccess(s)
        case _ => DynamoReadFailure(Seq(s"$value is not a String"))
      }
    }
  }

  implicit def readsOption[T](implicit r: DynamoReads[T]) = new DynamoReads[Option[T]] {
    override def reads(dynamoValue: DynamoValue): DynamoReadResult[Option[T]] = {
      r.reads(dynamoValue) match {
        case DynamoReadSuccess(t) => DynamoReadSuccess(Option(t))
        case DynamoReadFailure(e) => DynamoReadSuccess(None)
      }
    }
  }

  implicit def readsT[T](implicit r: DynamoReads[T]) = new DynamoReads[T] {
    override def reads(data: DynamoValue): DynamoReadResult[T] = r.reads(data)
  }
}