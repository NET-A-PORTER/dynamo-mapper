package com.netaporter.dynamomapper

trait DynamoReadable {
  def as[T](implicit reads: DynamoReads[T]): DynamoReadResult[T]
}
