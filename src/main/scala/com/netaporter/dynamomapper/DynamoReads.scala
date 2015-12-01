package com.netaporter.dynamomapper

trait DynamoReads[T] {
  def reads(dynamoValue: DynamoValue): DynamoReadResult[T]
}
