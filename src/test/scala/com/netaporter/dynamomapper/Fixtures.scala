package com.netaporter.dynamomapper

import com.netaporter.dynamomapper.DynamoMapper._

trait Fixtures {
  case class SimpleCaseClass(id: String, name: String)

  object SimpleCaseClass {
    implicit val format = writeFormat[SimpleCaseClass]
  }
  case class NestedCaseClass(id: String, simple: SimpleCaseClass)

  object NestedCaseClass {
    implicit val format = writeFormat[NestedCaseClass]
  }

  case class ClassWithMap(id: String, map: Map[String, String])

  object ClassWithMap {
    implicit val format = writeFormat[ClassWithMap]
  }

}
