package com.github.cjwebb.dynamomapper

import com.github.cjwebb.dynamomapper.Formats._

trait Fixtures {
  case class SimpleCaseClass(id: String, name: String)

  object SimpleCaseClass {
    implicit val writeFormat = new DynamoWrites[SimpleCaseClass] {
      override def writes(c: SimpleCaseClass): DynamoValue = map("id" -> c.id, "name" -> c.name)
    }
  }

  case class NestedCaseClass(id: String, simple: SimpleCaseClass)

  object NestedCaseClass {
    implicit val writeFormat = new DynamoWrites[NestedCaseClass] {
      override def writes(c: NestedCaseClass): DynamoValue = map("id" -> c.id, "simple" -> c.simple)
    }
  }

  case class ClassWithMap(id: String, map: Map[String, String])

  object ClassWithMap {
    implicit val writeFormat = new DynamoWrites[ClassWithMap] {
      override def writes(c: ClassWithMap): DynamoValue = map("id" -> c.id, "map" -> c.map)
    }
  }

}
