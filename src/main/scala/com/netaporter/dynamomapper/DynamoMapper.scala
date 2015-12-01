package com.netaporter.dynamomapper

import com.amazonaws.services.dynamodbv2.model.AttributeValue

import scala.collection.JavaConverters._

object DynamoMapper extends DefaultDynamoWrites with DefaultDynamoReads {
  /**
   * The type expected by the Java SDK for most 'Item' operations
   */
  type DynamoData = java.util.Map[String, AttributeValue]

  implicit def writes[T](t: T)(implicit w: DynamoWrites[T]): DynamoValue = w.writes(t)

  implicit def reads[T](v: DynamoValue)(implicit r: DynamoReads[T]): DynamoReadResult[T] = r.reads(v)

  /**
   * Creates a DynamoWrites[T] by resolving case class fields and values,
   * and required implicits at compile time.
   *
   * {{{
   *   import DynamoMapper._
   *
   *   case class User(name: String, email: String)
   *
   *   implicit val userWrites = DynamoMapper.writeFormat[User]
   *   // macro-compiler will expand this to:
   *   implicit val userWrites = new DynamoWrites[User] {
   *     override def writes(o: User) = map("name" -> o.name, "email" -> o.email)
   *   }
   * }}}
   */
  def writeFormat[T]: DynamoWrites[T] = macro FormatsMacroImpl.formatWriteImpl[T]

  /**
   * Creates a DynamoReads[T] by resolving case class fields and values,
   * and required implicits at compile time.
   *
   * {{{
   *   import DynamoMapper._
   *
   *   case class User(name: String, email: String)
   *
   *   implicit val userReads = DynamoMapper.readFormat[User]
   *   // macro-compiler will expand this to:
   *   implicit val userReads = new DynamoReads[User] {
   *     override def reads(d: DynamoValue): DynamoReadResult[User] =
   *       for {
   *         name  <- d.attr[String]("name")
   *         email <- d.attr[String]("email")
   *       } yield User(name, email)
   *   }
   * }}}
   */
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
   * @throws IllegalArgumentException if not given a [[DynamoMap]], as that's is what the Java SDK
   *                                  requires at the top-level.
   */
  def toDynamo(d: DynamoValue): DynamoData = {
    def filterNulls(m: Map[String, DynamoValue]) = m.filter{ case (k, v) => v != DynamoNull }

    def convert(value: DynamoValue): AttributeValue = {
      value match {
        case DynamoString(s) => new AttributeValue(s)
        case DynamoNumber(n) => new AttributeValue().withN(n)
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
      def isN: Boolean = Option(a.getN).isDefined

      if (isM) DynamoMap(a.getM.asScala.mapValues(convert).toMap)
      else if (isL) DynamoList(a.getL.asScala.map(convert).toList)
      else if (isN) DynamoNumber(a.getN)
      else DynamoString(a.getS)
    }

    DynamoMap(data.asScala.mapValues(convert).toMap)
  }
}
