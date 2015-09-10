package com.github.cjwebb.dynamomapper

import com.amazonaws.services.dynamodbv2.model.AttributeValue

import scala.annotation.implicitNotFound

object Formats {

  type DynamoData = java.util.Map[String, AttributeValue]

  @implicitNotFound("No DynamoWrites serializer found for type ${T}. Try to implement one.")
  trait DynamoWrites[T] {
    def writes(o: T): DynamoData
  }
  def writes[T](o: T)(implicit w: DynamoWrites[T]): DynamoData = w.writes(o)

  def writeFormat[T] = macro FormatsMacroImpl.formatWriteImpl[T]

}
