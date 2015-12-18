package com.netaporter.dynamomapper

import scala.annotation.implicitNotFound

import scala.language.higherKinds

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

  implicit def readsSeq[T](implicit r: DynamoReads[T]) = new DynamoReads[Seq[T]] {
    override def reads(value: DynamoValue): DynamoReadResult[Seq[T]] = {
      value match {
        case DynamoList(l) => DynamoReadResult.sequence(l.map(r.reads))
        case _ => DynamoReadFailure(Seq(s"$value is not a Seq"))
      }
    }
  }

  implicit object StringSetReads extends DynamoReads[Set[String]] {
    override def reads(dynamoValue: DynamoValue): DynamoReadResult[Set[String]] = {
      dynamoValue match {
        case DynamoStringSet(s) => DynamoReadSuccess(s)
        case _ => DynamoReadFailure(Seq(s"$dynamoValue is not Set[String]"))
      }
    }
  }

  // todo - can we remove these, and provide Iterable, or Traversable implementations?
  implicit def readsList[T](implicit r: DynamoReads[T]) = new DynamoReads[List[T]] {
    override def reads(value: DynamoValue): DynamoReadResult[List[T]] = {
      value match {
        case DynamoList(l) => DynamoReadResult.sequence(l.map(r.reads).toList)
        case _ => DynamoReadFailure(List(s"$value is not a Seq"))
      }
    }
  }
}