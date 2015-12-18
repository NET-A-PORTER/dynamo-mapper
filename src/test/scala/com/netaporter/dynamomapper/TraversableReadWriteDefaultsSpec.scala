package com.netaporter.dynamomapper

import com.netaporter.dynamomapper.DynamoMapper._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FreeSpec, Matchers}

/**
 * Checks that we can read and write the Scala Traversable traits,
 * such as Seq, List, and Set.
 *
 */
// todo - this is not yet a complete set of them. fix that.
class TraversableReadWriteDefaultsSpec extends FreeSpec with Matchers with PropertyChecks {

  "converting fromDynamo and toDynamo work on" - {

    "Seq[T]" in {
      forAll("Seq") { (l: Seq[String]) =>
        val m = map("l" -> l)
        fromDynamo(toDynamo(m)) shouldBe m
        m.attr[Seq[String]]("l") shouldBe DynamoReadSuccess(l)
      }
    }

    "List[T]" in {
      forAll("List") { (l: List[String]) =>
        val m = map("l" -> l)
        fromDynamo(toDynamo(m)) shouldBe m
        m.attr[List[String]]("l") shouldBe DynamoReadSuccess(l)
      }
    }

    "Set[String]" in {
      forAll("Set") { (l: Set[String]) =>
        val m = map("l" -> l)
        fromDynamo(toDynamo(m)) shouldBe m
        m.attr[Set[String]]("l") shouldBe DynamoReadSuccess(l)
      }
    }

    "Seq[Option[_]]" in {
      forAll("Seq[Option[String]]") { (l: Seq[Option[String]]) =>
        val m = map("l" -> l)
        fromDynamo(toDynamo(m)) shouldBe m
        m.attr[Seq[Option[String]]]("l") shouldBe DynamoReadSuccess(l)
      }
    }

  }

}
