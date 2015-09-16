package com.netaporter.dynamomapper

import scala.language.higherKinds
import scala.reflect.macros._
import scala.language.experimental.macros

object FormatsMacroImpl {

  def formatWriteImpl[T: c.WeakTypeTag](c: whitebox.Context): c.Expr[DynamoWrites[T]] = {
    import c.universe._

    val tpe = weakTypeOf[T]

    val fields = tpe.decls.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor => m
    }.get.paramLists.head

    val toMapParams = fields.map { field =>
      val name = field.name.toTermName
      val decoded = name.decodedName.toString
      q"($decoded -> o.$name)"
    }

    c.Expr[DynamoWrites[T]] { q"""
      new DynamoWrites[$tpe] {
        override def writes(o: $tpe): DynamoValue = map(..$toMapParams)
      }
      """
    }
  }
}
