package com.netaporter.dynamomapper

import scala.annotation.implicitNotFound

/**
 * Base trait that needs to be implemented for any T that needs reading from to DynamoDB.
 * Typically, this is an implicit.
 *
 * @tparam T the type to read from DynamoDB
 */
@implicitNotFound("No DynamoReads serializer found for type ${T}. Try to implement one.")
trait DynamoReads[T] {
  def reads(dynamoValue: DynamoValue): DynamoReadResult[T]
}

trait DefaultDynamoReads {

  implicit object StringReads extends DynamoReads[String] {
    override def reads(value: DynamoValue): DynamoReadResult[String] = value match {
      case DynamoString(s) => DynamoReadSuccess(s)
      case _ => DynamoReadFailure(Seq(s"$value is not a String"))
    }
  }

  implicit object IntReads extends DynamoReads[Int] {
    override def reads(value: DynamoValue): DynamoReadResult[Int] = value match {
      case DynamoNumber(n) => DynamoReadSuccess(n.toInt)
      case _ => DynamoReadFailure(Seq(s"$value is not an Int"))
    }
  }

  implicit object DoubleReads extends DynamoReads[Double] {
    override def reads(value: DynamoValue): DynamoReadResult[Double] = value match {
      case DynamoNumber(s) => DynamoReadSuccess(s.toDouble)
      case _ => DynamoReadFailure(Seq(s"$value is not a Double"))
    }
  }

  implicit object FloatReads extends DynamoReads[Float] {
    override def reads(value: DynamoValue): DynamoReadResult[Float] = value match {
      case DynamoNumber(s) => DynamoReadSuccess(s.toFloat)
      case _ => DynamoReadFailure(Seq(s"$value is not a Float"))
    }
  }

  implicit object LongReads extends DynamoReads[Long] {
    override def reads(value: DynamoValue): DynamoReadResult[Long] = value match {
      case DynamoNumber(s) => DynamoReadSuccess(s.toLong)
      case _ => DynamoReadFailure(Seq(s"$value is not a Long"))
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