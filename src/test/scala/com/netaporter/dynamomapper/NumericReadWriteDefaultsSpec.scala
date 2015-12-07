package com.netaporter.dynamomapper

import com.netaporter.dynamomapper.DynamoMapper._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FreeSpec, Matchers}

/**
 * Checks that we can read and write all the Scala numbers
 * by using [[DynamoNumber]] implicits
 */
class NumericReadWriteDefaultsSpec extends FreeSpec with Matchers with PropertyChecks {

  "converting fromDynamo and toDynamo work on" - {
    "Int" in {
      forAll("Int") { (n: Int) =>
        val m = map("n" -> n)
        fromDynamo(toDynamo(m)) shouldBe m
        m.attr[Int]("n") shouldBe DynamoReadSuccess(n)
      }
    }

    "Double" in {
      forAll("Double") { (n: Double) =>
        val m = map("n" -> n)
        fromDynamo(toDynamo(m)) shouldBe m
        m.attr[Double]("n") shouldBe DynamoReadSuccess(n)
      }
    }

    "Float" in {
      forAll("Float") { (n: Float) =>
        val m = map("n" -> n)
        fromDynamo(toDynamo(m)) shouldBe m
        m.attr[Float]("n") shouldBe DynamoReadSuccess(n)
      }
    }

    "Long" in {
      forAll("Long") { (n: Long) =>
        val m = map("n" -> n)
        fromDynamo(toDynamo(m)) shouldBe m
        m.attr[Long]("n") shouldBe DynamoReadSuccess(n)
      }
    }
  }
}
