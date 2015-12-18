package com.netaporter.dynamomapper

import scala.annotation.implicitNotFound

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

  implicit def listWritesT[T](implicit w: DynamoWrites[T]) = new DynamoWrites[List[T]] {
    override def writes(o: List[T]): DynamoValue = {
      DynamoList(o.map(v => w.writes(v)))
    }
  }

  implicit object StringSetWrites extends DynamoWrites[Set[String]] {
    override def writes(o: Set[String]): DynamoValue = {
      DynamoStringSet(o)
    }
  }

  implicit def writesOptionT[T](implicit w: DynamoWrites[T]) = new DynamoWrites[Option[T]] {
    override def writes(o: Option[T]): DynamoValue = {
      o.map(t => w.writes(t)).getOrElse(DynamoNull)
    }
  }

  implicit object IntWrites extends DynamoWrites[Int] {
    override def writes(i: Int): DynamoValue = DynamoNumber(i.toString)
  }

  implicit object DoubleWrites extends DynamoWrites[Double] {
    override def writes(d: Double): DynamoValue = DynamoNumber(d.toString)
  }

  implicit object FloatWrites extends DynamoWrites[Float] {
    override def writes(d: Float): DynamoValue = DynamoNumber(d.toString)
  }

  implicit object LongWrites extends DynamoWrites[Long] {
    override def writes(d: Long): DynamoValue = DynamoNumber(d.toString)
  }
}