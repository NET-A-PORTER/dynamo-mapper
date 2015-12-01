package com.netaporter.dynamomapper

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
