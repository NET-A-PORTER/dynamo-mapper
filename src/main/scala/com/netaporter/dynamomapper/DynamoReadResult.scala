package com.netaporter.dynamomapper

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds

sealed trait DynamoReadResult[+A] {
  def get: A
  def isSuccess: Boolean
  def isFailure: Boolean = !isSuccess

  // todo - accumulate errors
  def flatMap[B](f: A => DynamoReadResult[B]): DynamoReadResult[B] = this match {
    case DynamoReadSuccess(a) => f(a)
    case e: DynamoReadFailure => e
  }

  // todo - accumulate errors
  def map[B](f: A => B): DynamoReadResult[B] = this match {
    case DynamoReadSuccess(a) => DynamoReadSuccess(f(a))
    case e: DynamoReadFailure => e
  }

  def foldLeft[B](z: B)(op: (B, A) => B): B = {
    var result = z
    this foreach (x => result = op(result, x))
    result
  }

  private def foreach[B](f: A => B): Unit = f(get)
}

object DynamoReadResult {

  // todo - docs
  def sequence[A, M[X] <: TraversableOnce[X]](in: M[DynamoReadResult[A]])
                                             (implicit cbf: CanBuildFrom[M[DynamoReadResult[A]], A, M[A]]): DynamoReadResult[M[A]] = {
    in.foldLeft(success(cbf(in))) {
      (fr, fa) => for {
        r <- fr
        a <- fa
      } yield r += a
    } map (_.result())
  }

  private def success[T](result: T): DynamoReadResult[T] = DynamoReadSuccess(result)
}

case class DynamoReadSuccess[A](value: A) extends DynamoReadResult[A] {
  def get: A = value
  def isSuccess = true
}

// todo - don't use String as the error message
case class DynamoReadFailure(errors: Seq[String]) extends DynamoReadResult[Nothing] {
  def get: Nothing = throw new NoSuchElementException("DynamoReadFailure.get")
  def isSuccess = false
}
