package com.netaporter.dynamomapper

import com.netaporter.dynamomapper.DynamoMapper._

trait Fixtures {
  case class SimpleCaseClass(id: String, name: String)

  object SimpleCaseClass {
    implicit val format = writeFormat[SimpleCaseClass]
    implicit val readFormat = new DynamoReads[SimpleCaseClass] {
      override def reads(d: DynamoValue): DynamoReadResult[SimpleCaseClass] = {
        for {
          id <- d.attr[String]("id")
          name <- d.attr[String]("name")
        } yield SimpleCaseClass(id, name)
      }
    }
  }
  case class NestedCaseClass(id: String, simple: SimpleCaseClass)

  object NestedCaseClass {
    implicit val format = writeFormat[NestedCaseClass]
    implicit val readFormats = readFormat[NestedCaseClass]
  }

  case class ClassWithMap(id: String, map: Map[String, String])

  object ClassWithMap {
    implicit val format = writeFormat[ClassWithMap]
  }

  case class ClassWithList(id: String, list: Seq[SimpleCaseClass])

  object ClassWithList {
    implicit val format = writeFormat[ClassWithList]
  }

  case class ClassWithStringList(id: String, list: Seq[String])

  object ClassWithStringList {
    implicit val format = writeFormat[ClassWithStringList]
  }

  case class ClassWithOption(id: String, name: String, optional: Option[String])

  object ClassWithOption {
    implicit val wf = writeFormat[ClassWithOption]
    implicit val rf = readFormat[ClassWithOption]
  }

}
